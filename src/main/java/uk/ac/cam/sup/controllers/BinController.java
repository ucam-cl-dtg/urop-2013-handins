package uk.ac.cam.sup.controllers;

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

    @Path("/create")
    @GET
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
    FixMe: delete everything from the bin
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
        if (!bin.canDelete(user, token)) {
            return Response.status(401).build();
        }

        // ToDo: Delete all answers

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

        // Create list of people who have access to the bin
        List<String> res = new LinkedList<String>();
        for (BinPermission binPermission: bin.getPermissions())
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

        // Get all submissions from the list
        @SuppressWarnings("unchecked")
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
    public Object splitSubmission (@PathParam("submissionId") long submissionId,
                                   @FormParam("id[]") long[] questionId,
                                   @FormParam("start[]") int[] startPage,
                                   @FormParam("end[]") int[] endPage) {

        // Set Hibernate and get user
        Session session = HibernateUtil.getSession();

        String user = UserHelper.getCurrentUser(request);

        // Get the unmarkedSubmission
        UnmarkedSubmission unmarkedSubmission = (UnmarkedSubmission) session.get(UnmarkedSubmission.class, submissionId);

        // Inject the pdf with the metadata needed to split it
        PDFManip pdfManip;
        try {
            pdfManip = new PDFManip(unmarkedSubmission.getFilePath());
        } catch (Exception e) {
            return Response.status(404).build();
        }
        for (int i = 0; i < questionId.length; i++)
            FilesManip.markPdf(pdfManip, user, (ProposedQuestion) session.get(ProposedQuestion.class, questionId[i]), startPage[i], endPage[i]);

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
     */
    @GET
    @Path("/{binId}/questions")
    public Object viewBinQuestions(@PathParam("binId") long binId) {

        // Set Hibernate and get user
        Session session = HibernateUtil.getSession();

        String user = UserHelper.getCurrentUser(request);

        Bin bin = (Bin) session.get(Bin.class, binId);

        if (!bin.canSeeBin(user))
            return Response.status(401).build();

        List<ProposedQuestion> questions = new LinkedList<ProposedQuestion>(bin.getQuestionSet());

        List<ImmutableMap> result = new LinkedList<ImmutableMap>();
        for (ProposedQuestion question: questions) {
            result.add(ImmutableMap.of("id", question.getId(),
                                       "name", question.getName(),
                                       "bin", binId));
        }

        return ImmutableMap.of("questions", result);
    }


}
