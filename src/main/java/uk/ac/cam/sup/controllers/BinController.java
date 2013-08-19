package uk.ac.cam.sup.controllers;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.apache.commons.io.FilenameUtils;
import org.hibernate.Session;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.jboss.resteasy.annotations.Form;
import org.jboss.resteasy.annotations.providers.multipart.MultipartForm;
import uk.ac.cam.sup.HibernateUtil;
import uk.ac.cam.sup.forms.FileUploadForm;
import uk.ac.cam.sup.forms.SplittingForm;
import uk.ac.cam.sup.helpers.UserHelper;
import uk.ac.cam.sup.models.*;
import uk.ac.cam.sup.tools.FilesManip;
import uk.ac.cam.sup.tools.PDFManip;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.File;
import java.util.*;

@Path("/bins")
@Produces("application/json")
public class BinController {

    @SuppressWarnings({"UnusedDeclaration"})
    @Context
    private HttpServletRequest request;

    @GET
    @Path("/create")
    public Object viewForCreateBin() {
        return Response.status(200).build();
    }

    /*
    Done

    Checked
     */
    @POST
    public Object addBin(@FormParam("questionSet") String questionSet ) {

        // Set Hibernate and get user
        Session session = HibernateUtil.getSession();

        String user = UserHelper.getCurrentUser(request);

        if (questionSet.trim().isEmpty())
            return Response.status(418).build();

        // Create a new bin
        Bin bin = new Bin(user, questionSet.trim());

        // Save and return bin details
        session.save(bin);

        return ImmutableMap.of("id", bin.getId(),
                               "token", bin.getToken());
    }

    /*
    Done

    Checked
     */
    @GET
    @Path("/{binId}")
    public Object viewBin(@PathParam("binId") long binId) {

        // Set Hibernate and get user and bin
        Session session = HibernateUtil.getSession();

        String user = UserHelper.getCurrentUser(request);

        Bin bin = (Bin) session.get(Bin.class, binId);

        // Check the existence of the bin
        if (bin == null)
            return Response.status(404).build();

        if (! bin.canSeeBin(user))
            return Response.status(401).build();

        // Return bin details
        return ImmutableMap.of("bin", ImmutableMap.of("id", bin.getId(),
                                                      "name", bin.getName(),
                                                      "token", bin.getToken(),
                                                       "archived", bin.isArchived()));
    }

    /*
    Done

    Checked
     */
    @POST
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces("application/json")
    @Path("/{binId}")
    public Object addSubmission(@MultipartForm FileUploadForm uploadForm,
                                @PathParam("binId") long binId) {

        if (uploadForm.file.length == 0)
            return Response.status(401).build();

        // Set Hibernate and get user and bin
        Session session = HibernateUtil.getSession();

        String user = UserHelper.getCurrentUser(request);

        Bin bin = (Bin) session.get(Bin.class, binId);

        // Check the existence of the bin
        if (bin == null)
            return Response.status(404).build();

        if (!bin.canAddSubmission(user))
            return Response.status(401).build();

        // New unmarkedSubmission to get id
        UnmarkedSubmission unmarkedSubmission = new UnmarkedSubmission();
        session.save(unmarkedSubmission);

        // Create directory
        String directory = "temp/" + user + "/submissions/answers/";
        File fileDirectory = new File(directory);
        //noinspection ResultOfMethodCallIgnored
        fileDirectory.mkdirs();

        // Save the submission
        String fileName = "submission_" + unmarkedSubmission.getId() + ".pdf";
        try {
            FilesManip.fileSave(uploadForm.file, directory + fileName);
        } catch (Exception e) {
            return Response.status(345).build();
        }

        // Add the submission to the database
        unmarkedSubmission.setFilePath(directory + fileName);
        unmarkedSubmission.setBin(bin);
        unmarkedSubmission.setOwner(user);

        session.update(unmarkedSubmission);

        return ImmutableMap.of("unmarkedSubmission", ImmutableMap.of("id", unmarkedSubmission.getId()),
                                                                     "bin", bin.getId());
    }

    /*
    Done

    Checked
     */
    @GET
    @Path("/{binId}/permissions/")
    public Object viewBinPermissionsList(@PathParam("binId") long binId) {

        // Set Hibernate and get user and bin
        Session session = HibernateUtil.getSession();

        String user = UserHelper.getCurrentUser(request);

        Bin bin = (Bin) session.get(Bin.class, binId);

        // Check the existence of the bin
        if (bin == null)
            return Response.status(404).build();

        if (!bin.isOwner(user))
            return Response.status(401).build();

        // Get the list of people who can access the bin
        @SuppressWarnings("unchecked")
        List<BinAccessPermission> permissions = session.createCriteria(BinAccessPermission.class)
                                                 .add(Restrictions.eq("bin", bin))
                                                 .addOrder(Order.asc("userCrsId"))
                                                 .list();

        // Create list of people who have access to the bin
        List<String> res = new LinkedList<String>();
        for (BinAccessPermission binPermission : permissions)
            res.add(binPermission.getUserCrsId());

        return ImmutableMap.of("users", res);
    }

    /*
    Done

    Checked
     */
    @GET
    @Path("/{binId}/submissions")
    public Object viewSubmissionList(@PathParam("binId") long binId) {

        // Set Hibernate and get user and bin
        Session session = HibernateUtil.getSession();

        String user = UserHelper.getCurrentUser(request);

        Bin bin = (Bin) session.get(Bin.class, binId);

        // Check the existence of the bin
        if (bin == null)
            return Response.status(404).build();

        if (!bin.canSeeBin(user))
            return Response.status(401).build();

        // Get all submissions from the list
        // noinspection unchecked
        List<UnmarkedSubmission> allUnmarkedSubmissions = session.createCriteria(UnmarkedSubmission.class)
                                                                 .add(Restrictions.eq("bin", bin))
                                                                 .add(Restrictions.eq("owner", user))
                                                                 .addOrder(Order.asc("id"))
                                                                 .list();

        // Filter all visible submissions and get their link and Id
        List<ImmutableMap<String, ?> > mapList = new LinkedList<ImmutableMap<String, ?>>();
        for (UnmarkedSubmission unmarkedSubmission : allUnmarkedSubmissions)
            if (bin.canSeeSubmission(user, unmarkedSubmission))
                mapList.add(ImmutableMap.of("link", unmarkedSubmission.getId(),
                                            "id", Long.toString(unmarkedSubmission.getId())));

        return ImmutableMap.of("submissions", mapList);
    }

    /*
    Done

    Checked
     */
    @POST
    @Path("/{binId}/submissions/{submissionId}")
    public Object splitSubmission (@PathParam("binId") long binId,
                                   @PathParam("submissionId") long submissionId,
                                   @Form SplittingForm split) {

        // Set Hibernate and get user
        Session session = HibernateUtil.getSession();

        String user = UserHelper.getCurrentUser(request);

        Bin bin = (Bin) session.get(Bin.class, binId);

        // Check the existence of the bin
        if (bin == null)
            return Response.status(404).build();

        if (!bin.canAddSubmission(user))
            return Response.status(401).build();

        // Get the unmarkedSubmission
        UnmarkedSubmission unmarkedSubmission = (UnmarkedSubmission) session.get(UnmarkedSubmission.class, submissionId);

        if (!unmarkedSubmission.getOwner().equals(user))
            return Response.status(401).build();

        for (Answer answer : unmarkedSubmission.getAllAnswers())
            if (answer.isDownloaded())
                return Response.status(401).build();

        // Delete all answers from the submission
        for (Answer answer : unmarkedSubmission.getAllAnswers()) {

            FilesManip.fileDelete(answer.getFilePath());
            session.delete(answer);

            if (answer.isLast()) {
                @SuppressWarnings("unchecked")
                List<Answer> altAnswers = session.createCriteria(Answer.class)
                                                 .add(Restrictions.eq("bin", answer.getBin()))
                                                 .add(Restrictions.eq("owner", answer.getOwner()))
                                                 .add(Restrictions.eq("question", answer.getQuestion()))
                                                 .addOrder(Order.desc("dateCreated"))
                                                 .list();

                if (altAnswers.size() > 0)
                    altAnswers.get(0).setLast(true);
            }
        }

        // Inject the pdf with the metadata needed to split it
        PDFManip pdfManip;
        try {
            pdfManip = new PDFManip(unmarkedSubmission.getOriginalFilePath());
        } catch (Exception e) {
            return Response.status(404).build();
        }

        // Create directory
        String directory = "temp/" + user + "/submissions/temp/";
        File fileDirectory = new File(directory);
        //noinspection ResultOfMethodCallIgnored
        fileDirectory.mkdirs();

        // Split questions
        List<Integer> startPageFinal = new LinkedList<Integer>();
        List<Integer> endPageFinal = new LinkedList<Integer>();
        List<String> pathList = new LinkedList<String>();

        // Adding the new File
        String splitName = unmarkedSubmission.getFilePath();
        splitName = FilenameUtils.removeExtension(splitName);
        unmarkedSubmission.setSplitFilePath(splitName + "a.pdf");

        int actualPage = 0;
        try {
            for (int i = 0; i < split.elements(); pathList.add(directory + "file" + i + ".pdf"), i++)
            {
                if (split.getStartPage(i) == split.getEndPage(i)) {

                    pdfManip.takeBox(split.getStartPage(i), split.getEndLoc(i), split.getStartLoc(i), directory + "file" + i + ".pdf");

                    actualPage++;
                    startPageFinal.add(actualPage);
                    endPageFinal.add(actualPage);
                }
                else {
                    pdfManip.takeBox(split.getStartPage(i), 0, split.getStartLoc(i), directory + "t1.pdf");
                    if (split.getStartPage(i) + 1 != split.getEndPage(i))
                        pdfManip.takePages(split.getStartPage(i) + 1, split.getEndPage(i) - 1, directory + "t2.pdf");
                    pdfManip.takeBox(split.getEndPage(i), split.getEndLoc(i), 1f, directory + "t3.pdf");

                    if (split.getStartPage(i) + 1 != split.getEndPage(i))
                        FilesManip.mergePdf(ImmutableList.of(directory + "t1.pdf", directory + "t2.pdf", directory + "t3.pdf"), directory + "file" + i + ".pdf");
                    else FilesManip.mergePdf(ImmutableList.of(directory + "t1.pdf", directory + "t3.pdf"), directory + "file" + i + ".pdf");

                    actualPage++;
                    startPageFinal.add(actualPage);
                    actualPage += (new PDFManip(directory + "file" + i + ".pdf")).getPageCount();
                    endPageFinal.add(actualPage);
                }
            }

            FilesManip.mergePdf(pathList, unmarkedSubmission.getFilePath());
            pdfManip.setFilePath(unmarkedSubmission.getFilePath());
        }
        catch (Exception e) {
            return Response.status(401).build();
        }

        // Mark simply
        for (int i = 0; i < split.elements(); i++)
            FilesManip.markPdf(pdfManip, user, (ProposedQuestion) session.get(ProposedQuestion.class, split.getQuestionId(i)), startPageFinal.get(i), endPageFinal.get(i));

        // Split the resulting pdf
        FilesManip.distributeSubmission(user, unmarkedSubmission);

        return Response.ok().build();
    }

    /*
    Done

    Checked
     */
    @GET
    @Path("/{binId}/questions")
    public Object viewBinQuestions(@PathParam("binId") long binId) {

        // Set Hibernate and get user
        Session session = HibernateUtil.getSession();

        String user = UserHelper.getCurrentUser(request);

        Bin bin = (Bin) session.get(Bin.class, binId);
        // Check the existence of the bin
        if (bin == null)
            return Response.status(404).build();

        if (!bin.canSeeBin(user))
            return Response.status(401).build();

        // Query for all the questions in the bin
        @SuppressWarnings("unchecked")
        List<ProposedQuestion> questions = session.createCriteria(ProposedQuestion.class)
                                                  .add(Restrictions.eq("bin", bin))
                                                  .addOrder(Order.asc("id"))
                                                  .list();

        // Create the list of questions as json
        List<ImmutableMap> result = new LinkedList<ImmutableMap>();
        for (ProposedQuestion question: questions) {
            result.add(ImmutableMap.of("id", question.getId(),
                                       "name", question.getName(),
                                       "bin", binId));
        }

        return ImmutableMap.of("questions", result);
    }
}
