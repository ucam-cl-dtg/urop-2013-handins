package uk.ac.cam.sup.controllers;

import com.google.common.collect.ImmutableMap;
import com.itextpdf.text.DocumentException;
import org.hibernate.Session;
import org.jboss.resteasy.annotations.providers.multipart.MultipartForm;
import uk.ac.cam.sup.HibernateUtil;
import uk.ac.cam.sup.exceptions.MetadataNotFoundException;
import uk.ac.cam.sup.forms.FileUploadForm;
import uk.ac.cam.sup.helpers.UserHelper;
import uk.ac.cam.sup.models.Answer;
import uk.ac.cam.sup.models.Bin;
import uk.ac.cam.sup.models.ProposedQuestion;
import uk.ac.cam.sup.models.UnmarkedSubmission;
import uk.ac.cam.sup.structures.Marking;
import uk.ac.cam.sup.tools.FilesManip;
import uk.ac.cam.sup.tools.PDFManip;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.File;
import java.io.IOException;
import java.util.*;

@Path ("/submission")
public class SubmissionController {

    @POST
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces("application/json")
    @Path("/bin/{binId}")
    public Object createSubmission(@MultipartForm FileUploadForm uploadForm, @PathParam("binId") long binId) throws IOException, MetadataNotFoundException, DocumentException {

        // Set Hibernate and get user
        Session session = HibernateUtil.getSession();

        String user = UserHelper.getCurrentUser();

        // Get Bin and check
        Bin bin = BinController.getBin(binId);

        if (bin == null)
            return Response.status(404).build();
        if (!bin.canAddSubmission(user))
            return Response.status(401).build();

        // New unmarkedSubmission and get id
        UnmarkedSubmission unmarkedSubmission = new UnmarkedSubmission();
        session.save(unmarkedSubmission);

        // Create dir
        String directory = "temp/" + user + "/submissions/answers/";
        File fileDirectory = new File(directory);
        fileDirectory.mkdirs();

        String fileName = "submission_" + unmarkedSubmission.getId() + ".pdf";

        FilesManip.fileSave(uploadForm.file, directory + fileName);

        unmarkedSubmission.setBin(bin);
        unmarkedSubmission.setOwner(user);
        unmarkedSubmission.setFilePath(directory + fileName);

        session.update(unmarkedSubmission);

        // todo: convert the received files

        PDFManip pdfManip = new PDFManip(directory + fileName);

        // ToDo: Redirect to splitting screen

        for (int i = 1; i <= pdfManip.getPageCount(); i++)
            FilesManip.markPdf(pdfManip, user, (ProposedQuestion) session.get(ProposedQuestion.class, (long) i), i, i);

        FilesManip.distributeSubmission(unmarkedSubmission);

        return ImmutableMap.of("unmarkedSubmission", ImmutableMap.of("id", unmarkedSubmission.getId(),
                                                                     "link", unmarkedSubmission.getId()));
    }

    @GET
    @Path("/bin/{binId}")
    @Produces("application/json")
    public Object listSubmissions(@PathParam("binId") long binId) {

        // Get user
        String user = UserHelper.getCurrentUser();

        // Get Bin and check
        Bin bin = BinController.getBin(binId);

        if (bin == null)
            return Response.status(404).build();

        List<UnmarkedSubmission> allUnmarkedSubmissions = new LinkedList<UnmarkedSubmission>(bin.getUnmarkedSubmissions());
        List<UnmarkedSubmission> accessibleUnmarkedSubmissions = new LinkedList<UnmarkedSubmission>();

        for (UnmarkedSubmission unmarkedSubmission : allUnmarkedSubmissions)
            if (bin.canSeeSubmission(user, unmarkedSubmission))
                accessibleUnmarkedSubmissions.add(unmarkedSubmission);

        List<ImmutableMap<String, ?> > mapList = new LinkedList<ImmutableMap<String, ?>>();

        for (UnmarkedSubmission unmarkedSubmission : accessibleUnmarkedSubmissions)
            mapList.add(ImmutableMap.of("link", unmarkedSubmission.getId(),
                                        "id", Long.toString(unmarkedSubmission.getId())));

        return ImmutableMap.of("submissions", mapList);
    }

    @GET
    @Path("/{submissionId}")
    @Produces("application/json")
    public Object seeSubmission(@PathParam("submissionId") long submissionId) {

        // Set Hibernate and get user
        Session session = HibernateUtil.getSession();

        String user = UserHelper.getCurrentUser();

        UnmarkedSubmission unmarkedSubmission = (UnmarkedSubmission) session.get(UnmarkedSubmission.class, submissionId);

        if (unmarkedSubmission == null)
            return Response.status(404).build();

        // Get Bin and check
        Bin bin = unmarkedSubmission.getBin();

        if (!bin.canSeeSubmission(user, unmarkedSubmission))
            return Response.status(401).build();

        List<ImmutableMap<String, String>> answerList = new LinkedList<ImmutableMap<String, String>>();
        for (Answer answer : unmarkedSubmission.getAllAnswers())
            answerList.add(ImmutableMap.of("question", answer.getQuestion().getName(), "link", "submission/" + answer.getId(), "bin", Long.toString(bin.getId())));

        return ImmutableMap.of("answers", answerList);
    }

    @GET
    @Path("/{submissionId}/{answerId}")
    @Produces("application/pdf")
    public Object getAnswer(@PathParam("submissionId") long submissionId, @PathParam("answerId") long answerId) {

        // Set Hibernate and get user
        Session session = HibernateUtil.getSession();

        String user = UserHelper.getCurrentUser();

        Answer answer = (Answer) session.get(Answer.class, answerId);

        if (answer == null)
            return Response.status(404).build();

        // Get Bin and check
        Bin bin = answer.getBin();

        if (!bin.canSeeAnswer(user, answer))
            return Response.status(401).build();

        return Response.ok(new File(answer.getFilePath())).build();
    }

    @GET
    @Path("/{submissionId}/download")
    @Produces("application/pdf")
    public Object getSubmission(@PathParam("submissionId") long submissionId) {

        // Set Hibernate and get user
        Session session = HibernateUtil.getSession();

        String user = UserHelper.getCurrentUser();

        UnmarkedSubmission unmarkedSubmission = (UnmarkedSubmission) session.get(UnmarkedSubmission.class, submissionId);

        if (unmarkedSubmission == null)
            return Response.status(404).build();

        // Get Bin and check
        Bin bin = unmarkedSubmission.getBin();

        if (!bin.canSeeSubmission(user, unmarkedSubmission))
            return Response.status(401).build();

        return Response.ok(new File(unmarkedSubmission.getFilePath())).build();
    }

    @DELETE
    @Path("/{submissionId}")
    @Produces("application/json")
    public Object deleteSubmission(@PathParam("submissionId") long submissionId) {

        // Set Hibernate and get user
        Session session = HibernateUtil.getSession();

        String user = UserHelper.getCurrentUser();

        UnmarkedSubmission unmarkedSubmission = (UnmarkedSubmission) session.get(UnmarkedSubmission.class, submissionId);

        if (unmarkedSubmission == null)
            return Response.status(404).build();

        // Get Bin and check
        Bin bin = unmarkedSubmission.getBin();

        if (!bin.canDeleteSubmission(user, unmarkedSubmission))
            return Response.status(401).build();

        for (Answer answer : unmarkedSubmission.getAllAnswers()) {
            FilesManip.fileDelete(answer.getFilePath());
            session.delete(answer);
        }

        FilesManip.fileDelete(unmarkedSubmission.getFilePath());
        session.delete(unmarkedSubmission);

        return Response.status(200).build();
    }
}
