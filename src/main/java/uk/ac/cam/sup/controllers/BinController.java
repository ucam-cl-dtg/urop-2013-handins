package uk.ac.cam.sup.controllers;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.hibernate.Session;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.jboss.resteasy.annotations.providers.multipart.MultipartForm;
import uk.ac.cam.sup.HibernateUtil;
import uk.ac.cam.sup.forms.FileUploadForm;
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

    /*
    Done

    Checked
     */
    @GET
    public Object viewBinList() {

        // Set Hibernate and get user
        Session session = HibernateUtil.getSession();

        String user = UserHelper.getCurrentUser(request);

        // Get list of bins
        @SuppressWarnings("unchecked")
        List<Bin> binList = session.createCriteria(Bin.class)
                                   .addOrder(Order.desc("id"))
                                   .list();

        // Filter all visible bins and return them
        List<Map<String, ?>> finalBinList = new LinkedList<Map<String, ?>>();
        for (Bin bin : binList)
            if (bin.canSeeBin(user))
                finalBinList.add(ImmutableMap.of("id", bin.getId(),
                                                 "name", bin.getName(),
                                                 "isArchived", bin.isArchived(),
                                                 "questions", bin.getQuestionCount()));

        return ImmutableMap.of("bins", finalBinList);
    }

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

        // Create a new bin
        Bin bin = new Bin(user, questionSet);

        // Save and return bin details
        session.save(bin);

        return ImmutableMap.of("id", bin.getId(),
                               "token", bin.getToken());
    }

    /*
    Done

    Checked
     */
    @DELETE
    @Path("/{binId}")
    public Response deleteBin(@PathParam("binId") long binId,
                              @QueryParam("token") String token) {

        // Set Hibernate and get user and bin
        Session session = HibernateUtil.getSession();

        String user = UserHelper.getCurrentUser(request);

        Bin bin = (Bin) session.get(Bin.class, binId);

        // Check the existence of the bin
        if (bin == null)
            return Response.status(404).build();

        if (!bin.canDelete(user, token) || bin.getUnmarkedSubmissions().size() > 0)
            return Response.status(401).build();

        // Delete all questions from the bin
        for (ProposedQuestion proposedQuestion : bin.getQuestionSet())
            session.delete(proposedQuestion);

        // Delete the permissions of the bin
        for (BinPermission binPermission : bin.getPermissions())
            session.delete(binPermission);

        // Delete the bin
        session.delete(bin);

        return Response.ok().build();
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
                                                      "token", bin.getToken()));
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
        List<BinPermission> permissions = session.createCriteria(BinPermission.class)
                                                 .add(Restrictions.eq("bin", bin))
                                                 .addOrder(Order.asc("user"))
                                                 .list();

        // Create list of people who have access to the bin
        List<String> res = new LinkedList<String>();
        for (BinPermission binPermission : permissions)
            res.add(binPermission.getUser());

        return ImmutableMap.of("users", res);
    }

    /*
    Done

    Checked
     */
    @POST
    @Path("/{binId}/permissions/")
    public Response addPermissions(@PathParam("binId") long binId,
                                   @FormParam("users[]") String[] newUsers,
                                   @QueryParam("token") String token) {

        // Set Hibernate and get user and bin
        Session session = HibernateUtil.getSession();

        String user = UserHelper.getCurrentUser(request);

        Bin bin = (Bin) session.get(Bin.class, binId);

        // Check the existence of the bin
        if (bin == null)
            return Response.status(404).build();

        if (!bin.canAddPermission(user, token))
            return Response.status(401).build();

        // Get all existing users
        Set<String> usersWithPermission = new TreeSet<String>();
        for (BinPermission perm: bin.getPermissions())
            usersWithPermission.add(perm.getUser());

        // Add the new users
        for (String newUser : newUsers)
            if (!usersWithPermission.contains(newUser))
                session.save(new BinPermission(bin, newUser));

        return Response.ok().build();
    }

    /*
    Done

    Checked
     */
    @DELETE
    @Path("/{binId}/permissions")
    public Response deletePermissions(@PathParam("binId") long binId,
                                      @QueryParam("users[]") String[] users,
                                      @QueryParam("token") String token) {

        // Set Hibernate and get user and bin
        Session session = HibernateUtil.getSession();

        String user = UserHelper.getCurrentUser(request);

        Bin bin = (Bin) session.get(Bin.class, binId);

        // Check the existence of the bin
        if (bin == null)
            return Response.status(404).build();

        if (!bin.canDeletePermission(user, token))
            return Response.status(401).build();

        // Get all BinPermissions
        @SuppressWarnings("unchecked")
        List<BinPermission> permissions = session.createCriteria(BinPermission.class)
                                                 .add(Restrictions.in("user", users))
                                                 .add(Restrictions.eq("bin", bin))
                                                 .list();

        // Delete all BinPermissions
        for (BinPermission perm: permissions)
            session.delete(perm);

        return Response.ok().build();
    }

    /*
    Done

    Checked
     */
    @GET
    @Path("/{binId}/submissions")
    @Produces("application/json")
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
                                   @FormParam("id[]") long[] questionId,
                                   @FormParam("startPage[]") int[] startPage,
                                   @FormParam("endPage[]") int[] endPage,
                                   @FormParam("startLoc[]") float[] startLoc,
                                   @FormParam("endLoc[]") float[] endLoc) {

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
        unmarkedSubmission.setFilePath("2.pdf");

        if (!unmarkedSubmission.getOwner().equals(user))
            return Response.status(401).build();

        // Inject the pdf with the metadata needed to split it
        PDFManip pdfManip;
        try {
            pdfManip = new PDFManip(unmarkedSubmission.getFilePath());
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

        int actualPage = 0;
        try {
            for (int i = 0; i < questionId.length; pathList.add(directory + "file" + i + ".pdf"), i++)
            {
                if (startPage[i] == endPage[i]) {

                    pdfManip.takeBox(startPage[i], endLoc[i], startLoc[i], directory + "file" + i + ".pdf");

                    actualPage++;
                    startPageFinal.add(actualPage);
                    endPageFinal.add(actualPage);
                }
                else {
                    pdfManip.takeBox(startPage[i], 0, startLoc[i], directory + "t1.pdf");
                    if (startPage[i] + 1 != endPage[i])
                        pdfManip.takePages(startPage[i] + 1, endPage[i] - 1, directory + "t2.pdf");
                    pdfManip.takeBox(endPage[i], endLoc[i], 1f, directory + "t3.pdf");

                    if (startPage[i] + 1 != endPage[i])
                        FilesManip.mergePdf(ImmutableList.of(directory + "t1.pdf", directory + "t2.pdf", directory + "t3.pdf"), directory + "file" + i + ".pdf");
                    else FilesManip.mergePdf(ImmutableList.of(directory + "t1.pdf", directory + "t3.pdf"), directory + "file" + i + ".pdf");

                    actualPage++;
                    startPageFinal.add(actualPage);
                    actualPage += (new PDFManip(directory + "file" + i + ".pdf")).getPageCount();
                    endPageFinal.add(actualPage);
                }
            }

            FilesManip.mergePdf(pathList, unmarkedSubmission.getFilePath());
        }
        catch (Exception e) {
            return Response.status(401).build();
        }

        // Mark simply
        for (int i = 0; i < questionId.length; i++)
            FilesManip.markPdf(pdfManip, user, (ProposedQuestion) session.get(ProposedQuestion.class, questionId[i]), startPageFinal.get(i), endPageFinal.get(i));

        // Split the resulting pdf
        FilesManip.distributeSubmission(user, unmarkedSubmission);

        return Response.ok().build();
    }

    /*
    Done

    Checked
     */
    @POST
    @Path("/{binId}/change")
    public Object changeArchiveBin(@PathParam("binId") long binId) {

        // Set Hibernate and get user and bin
        Session session = HibernateUtil.getSession();

        String user = UserHelper.getCurrentUser(request);

        Bin bin = (Bin) session.get(Bin.class, binId);

        // Check the existence of the bin
        if (bin == null)
            return Response.status(404).build();

        if (!bin.isOwner(user))
            return Response.status(401).build();

        // Change the archive
        bin.setArchived(!bin.isArchived());

        session.update(bin);

        return Response.ok().build();
    }

    /*
    Done

    Checked
     */
    @POST
    @Path("/{binId}/questions")
    public Object addBinQuestion(@PathParam("binId") long binId,
                                 @FormParam("questionName") String questionName) {

        // Set Hibernate and get user
        Session session = HibernateUtil.getSession();

        String user = UserHelper.getCurrentUser(request);

        Bin bin = (Bin) session.get(Bin.class, binId);

        // Check the existence of the bin
        if (bin == null)
            return Response.status(404).build();

        if (!bin.isOwner(user))
            return Response.status(401).build();

        // New ProposedQuestion to get Id
        ProposedQuestion question = new ProposedQuestion();
        session.save(question);

        // Add the question to the database
        question.setName(questionName);
        question.setBin(bin);

        session.update(question);

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

    /*
    Done

    Checked
     */
    @DELETE
    @Path("/{binId}/questions/{questionId}")
    public Object deleteBinQuestions(@PathParam("binId") long binId,
                                     @PathParam("questionId[]") long[] questionIds) {

        // Set Hibernate and get user
        Session session = HibernateUtil.getSession();

        String user = UserHelper.getCurrentUser(request);

        Bin bin = (Bin) session.get(Bin.class, binId);

        // Check the existence of the bin
        if (bin == null)
            return Response.status(404).build();

        if (!bin.isOwner(user))
            return Response.status(401).build();

        // Get the question and delete it or add it to the list of undeletable
        List<ImmutableMap> unDel = new LinkedList<ImmutableMap>();
        for (long questionId : questionIds) {
            ProposedQuestion proposedQuestion = (ProposedQuestion) session.get(ProposedQuestion.class, questionId);

            // Check if has any answers
            if (proposedQuestion.getAnswers().size() > 0)
                unDel.add(ImmutableMap.of("id", questionId,
                                          "name", proposedQuestion.getName()));
            else session.delete(proposedQuestion);
        }

        if (unDel.size() > 0)
            return ImmutableMap.of("unDel", unDel);
        return Response.ok().build();
    }
}
