package uk.ac.cam.sup.controllers;

import com.google.common.collect.ImmutableMap;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;
import org.jboss.resteasy.annotations.Form;
import org.jboss.resteasy.annotations.providers.multipart.MultipartForm;
import uk.ac.cam.sup.HibernateUtil;
import uk.ac.cam.sup.forms.BinForm;
import uk.ac.cam.sup.forms.FileUploadForm;
import uk.ac.cam.sup.helpers.UserHelper;
import uk.ac.cam.sup.models.Bin;
import uk.ac.cam.sup.models.BinAccessPermission;
import uk.ac.cam.sup.models.BinMarkingPermission;
import uk.ac.cam.sup.models.ProposedQuestion;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

@Path("/bins")
@Produces("application/json")
public class BinEditor {

    @SuppressWarnings({"UnusedDeclaration"})
    @Context
    private HttpServletRequest request;

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
        for (BinAccessPermission binPermission : bin.getAccessPermissions())
            session.delete(binPermission);

        // Delete the bin
        session.delete(bin);

        return Response.ok().build();
    }

    /*
    Done

    Checked
     */
    @PUT
    @Path("/{binId}")
    public Object changeBin(@PathParam("binId") long binId,
                            @Form BinForm binForm) {

        // Set Hibernate and get user and bin
        Session session = HibernateUtil.getSession();

        String user = UserHelper.getCurrentUser(request);

        Bin bin = (Bin) session.get(Bin.class, binId);

        // Check the existence of the bin
        if (bin == null)
            return Response.status(404).build();

        if (!bin.isOwner(user))
            return Response.status(401).build();

        // Update the bin accordingly
        binForm.save(bin);
        session.update(bin);

        return Response.ok().build();
    }

    /*
    Done

    Checked
     */
    @POST
    @Path("/{binId}/permissions")
    public Response addBinAccessPermissions(@PathParam("binId") long binId,
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
        for (BinAccessPermission perm: bin.getAccessPermissions())
            usersWithPermission.add(perm.getUserCrsId());

        // Add the new users
        for (String newUser : newUsers) {
            newUser = newUser.trim();

            if (!newUser.isEmpty() && !usersWithPermission.contains(newUser))
                session.save(new BinAccessPermission(bin, newUser));
        }

        return Response.ok().build();
    }

    /*
    Done

    Checked
     */
    @DELETE
    @Path("/{binId}/permissions")
    public Response deleteBinAccessPermissions(@PathParam("binId") long binId,
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
        List<BinAccessPermission> permissions = session.createCriteria(BinAccessPermission.class)
                                                       .add(Restrictions.in("userCrsId", users))
                                                       .add(Restrictions.eq("bin", bin))
                                                       .list();

        // Delete all BinPermissions
        for (BinAccessPermission perm: permissions)
            session.delete(perm);

        return Response.ok().build();
    }

    /*
    Done

    Checked
     */
    @POST
    @Path("/{binId}/marking-permissions")
    public Response addBinMarkingPermissions(@PathParam("binId") long binId,
                                             @FormParam("markingUsers[]") String[] markingUsers,
                                             @FormParam("questionIds[]") long[] questionIds,
                                             @FormParam("questionOwners[]") String[] questionOwners,
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

        for (String markingUser : markingUsers)
            for (long questionId : questionIds)
                for (String questionOwner : questionOwners) {

                    // Check if marking permissions already exists and skip if it does
                    if (session.createCriteria(BinMarkingPermission.class)
                               .add(Restrictions.eq("bin", bin))
                               .add(Restrictions.eq("userCrsId", markingUser))
                               .add(Restrictions.eq("questionId", questionId))
                               .add(Restrictions.eq("questionOwner", questionOwner))
                               .list().size() > 0)
                        continue;

                    // Add the new user permission
                    session.save(new BinMarkingPermission(bin, markingUser, questionId, questionOwner));
                }

        return Response.ok().build();
    }

    /*
    Done

    Checked
     */
    @DELETE
    @Path("/{binId}/marking-permissions")
    public Response deleteBinMarkingPermissions(@PathParam("binId") long binId,
                                                @QueryParam("markingUsers[]") String[] markingUsers,
                                                @QueryParam("questionIds[]") long[] questionIds,
                                                @QueryParam("questionOwners[]") String[] questionOwners,
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

        for (String markingUser : markingUsers)
            for (long questionId : questionIds)
                for (String questionOwner : questionOwners) {

                    // Check if marking permissions already exists and skip if it does
                    List permissions = session.createCriteria(BinMarkingPermission.class)
                                              .add(Restrictions.eq("bin", bin))
                                              .add(Restrictions.eq("userCrsId", markingUser))
                                              .add(Restrictions.eq("questionId", questionId))
                                              .add(Restrictions.eq("questionOwner", questionOwner))
                                              .list();

                    if (permissions.size() == 0)
                        continue;

                    // Add the new user permission
                    session.delete(permissions.get(0));
                }

        return Response.ok().build();
    }

    /*
    Done

    Checked
     */
    @POST
    @Path("/{binId}/questions")
    public Object addBinQuestions(@PathParam("binId") long binId,
                                  @FormParam("name[]") String[] newQuestionNames,
                                  @FormParam("name") String questionName) {

        if (questionName != null && (newQuestionNames == null || newQuestionNames.length == 0))
            newQuestionNames = new String[] {questionName};

        // Set Hibernate and get user
        Session session = HibernateUtil.getSession();

        String user = UserHelper.getCurrentUser(request);

        Bin bin = (Bin) session.get(Bin.class, binId);

        // Check the existence of the bin
        if (bin == null)
            return Response.status(404).build();

        if (!bin.isOwner(user))
            return Response.status(401).build();

        // Get all existing questions
        Set<String> existingQuestions = new TreeSet<String>();
        for (ProposedQuestion question : bin.getQuestionSet())
            existingQuestions.add(question.getName());

        // Add the new questions
        for (String newQuestion : newQuestionNames) {
            newQuestion = newQuestion.trim();

            if (!newQuestion.isEmpty() && !existingQuestions.contains(newQuestion)) {
                ProposedQuestion question = new ProposedQuestion();
                session.save(question);

                // Add the question details to the database
                question.setName(newQuestion);
                question.setBin(bin);

                session.update(question);
            }
        }

        return Response.ok().build();
    }


    @DELETE
    @Path("/{binId}/questions/{questionId}")
    public Object deleteBinQuestion(@PathParam("binId") long binId,
                                    @PathParam("questionId") long questionId) {
        long[] questions = {questionId};
        return deleteBinQuestions(binId, questions);
    }
    /*
    Done

    Checked
     */
    @DELETE
    @Path("/{binId}/questions")
    public Object deleteBinQuestions(@PathParam("binId") long binId,
                                     @QueryParam("questionId[]") long[] questionIds) {

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
