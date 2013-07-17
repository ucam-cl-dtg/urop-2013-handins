package uk.ac.cam.sup.controllers;

import com.google.common.collect.ImmutableMap;
import org.hibernate.Session;
import org.jboss.resteasy.annotations.providers.multipart.MultipartForm;
import uk.ac.cam.sup.HibernateUtil;
import uk.ac.cam.sup.exceptions.MetadataNotFoundException;
import uk.ac.cam.sup.forms.FileUploadForm;
import uk.ac.cam.sup.helpers.UserHelper;
import uk.ac.cam.sup.models.Answer;
import uk.ac.cam.sup.models.Bin;
import uk.ac.cam.sup.models.Submission;
import uk.ac.cam.sup.tools.FilesManip;
import uk.ac.cam.sup.tools.PDFManip;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.File;
import java.io.IOException;
import java.util.*;

@Path ("/submission/{binId}")
@Produces("application/json")
public class SubmissionController {

    @POST
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public Object createSubmission(@MultipartForm FileUploadForm uploadForm, @PathParam("binId") long binId) {

        // Set Hibernate and get user
        Session session = HibernateUtil.getSession();

        String user = UserHelper.getCurrentUser();

        // Get Bin and check
        Bin bin = BinController.getBin(binId);

        if (bin == null)
            return Response.status(404).build();
        if (!bin.canAddSubmission(user))
            return Response.status(401).build();

        // New submission and get id
        Submission submission = new Submission();
        session.save(submission);

        session.getTransaction().commit();

        // Restart session
        session = HibernateUtil.getSession();
        session.beginTransaction();

        String directory = "temp/" + user + "/submissions/answers/";
        String fileName = "submission_" + submission.getId() + ".pdf";

        try {
            FilesManip.fileSave(uploadForm.file, directory + fileName);
        } catch (IOException e) {
            e.printStackTrace();

            return Response.status(500).build();
        }

        submission.setBin(bin);
        submission.setOwner(user);
        submission.setFilePath(directory + fileName);

        session.update(submission);

        // todo: convert the received file;

        PDFManip.PdfMetadataInject("uploader", user, directory + fileName);

        // ToDo: Redirect to splitting screen

        PDFManip.PdfMetadataInject("page.1", "qqq 1", directory + fileName);
        PDFManip.PdfMetadataInject("page.2", "qqq 1", directory + fileName);
        PDFManip.PdfMetadataInject("page.3", "ExampleSheet", directory + fileName);
        PDFManip.PdfMetadataInject("page.4", "qqq 5", directory + fileName);
        PDFManip.PdfMetadataInject("page.5", "qqq 5", directory + fileName);

        FilesManip.distributeSubmission(submission);

        return ImmutableMap.of("submission", ImmutableMap.of("id", submission.getId(),
                                                             "link", submission.getLink(),
                                                             "filePath", submission.getFilePath()));
    }

    @GET
    public Object listSubmissions(@PathParam("binId") long binId) {

        // Set Hibernate and get user
        Session session = HibernateUtil.getSession();

        String user = UserHelper.getCurrentUser();

        // Get Bin and check
        Bin bin = BinController.getBin(binId);

        if (bin == null)
            return Response.status(404).build();

        List<Submission> allSubmissions = new LinkedList<Submission>(bin.getSubmissions());
        List<Submission> accessibleSubmissions = new LinkedList<Submission>();

        for (Submission submission : allSubmissions)
            if (bin.canSeeSubmission(user, submission))
                accessibleSubmissions.add(submission);

        List<Map<String, String> > mapList = new LinkedList<Map<String, String>>();

        int p = 0;
        for (Submission submission : accessibleSubmissions) {

            Map<String, String> map = new HashMap<String, String>();

            map.put("id", Long.toString(submission.getId()));
            map.put("filePath", submission.getFilePath());
            map.put("link", submission.getLink());

            mapList.add(map);
        }

        return ImmutableMap.of("submissions", mapList);
    }

    @GET
    @Path("/{submissionId}")
    public Object seeSubmission(@PathParam("binId") long binId, @PathParam("submissionId") long submissionId) {

        // Set Hibernate and get user
        Session session = HibernateUtil.getSession();

        String user = UserHelper.getCurrentUser();

        // Get Bin and check
        Bin bin = BinController.getBin(binId);

        if (bin == null)
            return Response.status(404).build();

        Submission submission = (Submission) session.get(Submission.class, submissionId);

        if (!bin.canSeeSubmission(user, submission))
            return Response.status(401).build();

        System.out.println(submission.getFilePath());

        return Response.ok(new File(submission.getFilePath())).build();
    }

    @DELETE
    @Path("/{submissionId}")
    public Object deleteSubmission(@PathParam("binId") long binId, @PathParam("submissionId") long submissionId) {

        // Set Hibernate and get user
        Session session = HibernateUtil.getSession();

        String user = UserHelper.getCurrentUser();

        // Get Bin and check
        Bin bin = BinController.getBin(binId);

        if (bin == null)
            return Response.status(404).build();

        Submission submission = (Submission) session.get(Submission.class, submissionId);

        if (session != null)
        {
            if (!bin.canDeleteSubmission(user, submission))
                return Response.status(401).build();

            session.delete(submission);
        }

        return Response.status(200).build();
    }
}
