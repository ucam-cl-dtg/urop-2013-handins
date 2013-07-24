package uk.ac.cam.sup.controllers;

import com.google.common.collect.ImmutableMap;
import com.itextpdf.text.DocumentException;
import org.hibernate.FetchMode;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;
import uk.ac.cam.sup.HibernateUtil;
import uk.ac.cam.sup.helpers.UserHelper;
import uk.ac.cam.sup.models.Bin;
import uk.ac.cam.sup.models.BinPermission;
import uk.ac.cam.sup.models.MarkedAnswer;
import uk.ac.cam.sup.models.ProposedQuestion;
import uk.ac.cam.sup.structures.Marking;
import uk.ac.cam.sup.tools.FilesManip;

import javax.ws.rs.*;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.*;

@Path("/bin")
@Produces("application/json")
public class BinController {

    /*
    ToDo: COMPLETE AND MOVE THE NEXT 2 FUNCTIONS
     */

    @GET
    @Path("/{binId}/marked/{markedAnswerId}/download")
    @Produces("application/pdf")
    public Object getMarkedAnswer(@PathParam("binId") long binId, @PathParam("markedAnswerId") long markedAnswerId) throws IOException, DocumentException {

        // Set Hibernate and get user
        Session session = HibernateUtil.getSession();

        String user = UserHelper.getCurrentUser();

        Bin bin = (Bin) session.get(Bin.class, binId);

        MarkedAnswer markedAnswer = (MarkedAnswer) session.get(MarkedAnswer.class, markedAnswerId);

        if (bin.canSeeAnnotated(user, markedAnswer)) {
            List<Marking> markedList = new LinkedList<Marking>();

            markedList.add(new Marking(markedAnswer.getFilePath()));

            return FilesManip.resultingFile(markedList);
        }

        return Response.status(401).build();
    }

    @GET
    @Path("/{binId}/marked/download")
    @Produces("application/pdf")
    public Object getMarkedAnswers(@PathParam("binId") long binId) throws IOException, DocumentException {

        // Set Hibernate and get user
        Session session = HibernateUtil.getSession();

        String user = UserHelper.getCurrentUser();

        Bin bin = (Bin) session.get(Bin.class, binId);

        List<MarkedAnswer> markedAnswers = session.createCriteria(MarkedAnswer.class)
                                                     .add(Restrictions.eq("bin", bin))
                                                     .add(Restrictions.eq("owner", user))
                                                     .list();

        List<Marking> markedList = new LinkedList<Marking>();


        for (MarkedAnswer markedAnswer : markedAnswers)
            if (bin.canSeeAnnotated(user, markedAnswer)) {

                markedList.add(new Marking(markedAnswer.getFilePath()));

                return FilesManip.resultingFile(markedList);
            }

        return Response.status(401).build();
    }

    public static Bin getBin(long id) {
        // Set Hibernate
        Session session = HibernateUtil.getSession();

        return (Bin) session.createCriteria(Bin.class)
                            .add(Restrictions.eq("id", id))
                            .setFetchMode("permissions", FetchMode.JOIN)
                            .uniqueResult();
    }

    @GET
    public Object listBins() {

        // Set Hibernate and get user
        Session session = HibernateUtil.getSession();

        String user = UserHelper.getCurrentUser();

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

    @DELETE
    @Path("/{binId}/")
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

    @GET
    @Path("/{id}")
    public Object showBin(@PathParam("id") long id) {
        Bin bin = getBin(id);

        if (bin == null)
            throw new NotFoundException();

        return ImmutableMap.of("bin", ImmutableMap.of(
                               "id", bin.getId(),
                               "name", bin.getName()));
    }

    @GET
    @Path("/{id}/permission/")
    public List<String> listPermissions(@PathParam("id") long id) {
        Bin bin = getBin(id);

        if (bin == null)
            throw new NotFoundException();

        List<String> res = new LinkedList<String>();

        for (BinPermission binPermission: bin.getPermissions()) {
            res.add(binPermission.getUser());
        }

        return res;
    }

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

    @DELETE
    @Path("/{id}/permission")
    public Response removePermissions(@PathParam("id") long id,
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
