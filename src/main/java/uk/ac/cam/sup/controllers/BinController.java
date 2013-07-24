package uk.ac.cam.sup.controllers;

import com.google.common.collect.ImmutableMap;
import com.itextpdf.text.DocumentException;
import org.hibernate.FetchMode;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;
import org.jboss.resteasy.annotations.providers.multipart.MultipartForm;
import uk.ac.cam.sup.HibernateUtil;
import uk.ac.cam.sup.exceptions.MetadataNotFoundException;
import uk.ac.cam.sup.forms.FileUploadForm;
import uk.ac.cam.sup.helpers.UserHelper;
import uk.ac.cam.sup.models.*;
import uk.ac.cam.sup.tools.FilesManip;
import uk.ac.cam.sup.tools.PDFManip;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.File;
import java.io.IOException;
import java.util.*;

@Path("/bin")
@Produces("application/json")
public class BinController {

    /*
    Done
     */
    public static Bin getBin(long id) {
        // Set Hibernate
        Session session = HibernateUtil.getSession();

        return (Bin) session.createCriteria(Bin.class)
                .add(Restrictions.eq("id", id))
                .setFetchMode("permissions", FetchMode.JOIN)
                .uniqueResult();
    }

    /*
    Done
     */
    @POST
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces("application/json")
    @Path("/{binId}")
    public Object addSubmission(@MultipartForm FileUploadForm uploadForm, @PathParam("binId") long binId) throws IOException, MetadataNotFoundException, DocumentException {

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
        //noinspection ResultOfMethodCallIgnored
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

    /*
    Done
     */
    @GET
    @Path("/{binId}")
    @Produces("application/json")
    public Object viewSubmissionList(@PathParam("binId") long binId) {

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

    /*
    Done
     */
    @GET
    public Object listBins() {

        // Set Hibernate and get user
        Session session = HibernateUtil.getSession();

        String user = UserHelper.getCurrentUser();

        @SuppressWarnings("unchecked")
        List<Bin> binList = session.createCriteria(Bin.class).list();
        List<Map<String, ?>> finalBinList = new LinkedList<Map<String, ?>>();

        for (Bin bin : binList)
            if (bin.canAddSubmission(user))
                finalBinList.add(ImmutableMap.of("id", bin.getId(),
                                                 "name", bin.getName(),
                                                 "isArchived", bin.isArchived(),
                                                 "questions", bin.getQuestionCount()));

        return ImmutableMap.of("bins", finalBinList);
    }

    /*
    Done
     */
    @POST
    public Map<String, ?> createBin(@FormParam("owner") String owner,
                                    @FormParam("questionSet") String questionSet ) {

        // Set Hibernate
        Session session = HibernateUtil.getSession();

        Bin bin = new Bin(owner, questionSet);

        session.save(bin);

        return ImmutableMap.of("id", bin.getId(),
                               "token", bin.getToken());
    }

    /*
    Done
     */
    @POST
    @Path("/{binId}/change")
    public Object changeArchiveBin(@PathParam("binId") long binId) {

        // Set Hibernate and get user
        Session session = HibernateUtil.getSession();

        String user = UserHelper.getCurrentUser();

        Bin bin = (Bin) session.get(Bin.class, binId);

        if (!bin.isOwner(user))
            return Response.status(401).build();

        bin.setArchived(!bin.isArchived());

        session.update(bin);

        return Response.ok().build();
    }

    /*
    Done
     */
    @POST
    @Path("/{binId}/add")
    public Object addBinQuestion(@PathParam("binId") long binId,
                                 @FormParam("questionName") String questionName) {

        // Set Hibernate and get user
        Session session = HibernateUtil.getSession();

        String user = UserHelper.getCurrentUser();

        Bin bin = (Bin) session.get(Bin.class, binId);

        if (!bin.isOwner(user))
            return Response.status(401).build();

        ProposedQuestion question = new ProposedQuestion();
        session.save(question);

        question.setName(questionName);
        question.setBin(bin);

        session.update(question);

        return Response.ok().build();
    }

    /*
    Done
     */
    @DELETE
    @Path("/{binId}")
    public Response deleteBin(@PathParam("binId") long binId,
                              @QueryParam("token") String token) {

        // Set Hibernate and get user
        Session session = HibernateUtil.getSession();

        String user = UserHelper.getCurrentUser();

        Bin bin = (Bin) session.get(Bin.class, binId);


        if (bin == null)
            return Response.status(404).build();
        if (!bin.canDelete(user, token)) {
            return Response.status(401).build();
        }

        session.delete(bin);

        return Response.ok().build();
    }

    /*
    Done
     */
    @GET
    @Path("/{id}")
    public Object viewBin(@PathParam("id") long id) {
        Bin bin = getBin(id);

        if (bin == null)
            throw new NotFoundException();

        return ImmutableMap.of("bin", ImmutableMap.of(
                               "id", bin.getId(),
                               "name", bin.getName()));
    }

    /*
    Done
     */
    @GET
    @Path("/{id}/permission/")
    public List<String> viewBinPermissionsList(@PathParam("id") long id) {
        Bin bin = getBin(id);

        if (bin == null)
            throw new NotFoundException();

        List<String> res = new LinkedList<String>();

        for (BinPermission binPermission: bin.getPermissions()) {
            res.add(binPermission.getUser());
        }

        return res;
    }

    /*
    Done
     */
    @POST
    @Path("/{id}/permission/")
    public Response addPermissions(@PathParam("id") long id,
                                   @FormParam("users[]") String[] users,
                                   @FormParam("token") String token) {
        Bin bin = getBin(id);

        if (bin == null)
            return Response.status(404).build();

        if (!bin.canAddPermission(token))
            return Response.status(401).build();

        Set<String> usersWithPermission = new HashSet<String>();
        for (BinPermission perm: bin.getPermissions()) {
            usersWithPermission.add(perm.getUser());
        }

        Session session = HibernateUtil.getSession();
        for (String user: users) {
            if (usersWithPermission.contains(user))
                continue;
            session.save(new BinPermission(bin, user));
        }

        return Response.ok().build();
    }

    /*
    Done
     */
    @DELETE
    @Path("/{id}/permission")
    public Response deletePermissions(@PathParam("id") long id,
                                      @QueryParam("users[]") String[] users,
                                      @QueryParam("token") String token) {
        Bin bin = getBin(id);

        if (bin == null)
            return Response.status(404).build();

        if (!bin.canDeletePermission(token))
            return Response.status(401).build();

        Session session = HibernateUtil.getSession();

        List permissions = session.createCriteria(BinPermission.class)
                                  .add(Restrictions.in("user", users))
                                  .add(Restrictions.eq("bin", bin))
                                  .list();

        for (Object perm: permissions) {
            session.delete(perm);
        }

        return Response.ok().build();
    }
}
