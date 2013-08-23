package uk.ac.cam.sup.controllers;

import com.google.common.collect.ImmutableMap;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;
import org.jboss.resteasy.annotations.Form;
import uk.ac.cam.sup.HibernateUtil;
import uk.ac.cam.sup.forms.BinForm;
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

        if (!bin.canDelete(user, token))
            return Response.status(403).entity(ImmutableMap.of("message", "Cannot delete bin.")).build();

        if (bin.getUnmarkedSubmissions().size() > 0)
            return Response.status(403).entity(ImmutableMap.of("message", "Contains uploaded files. Unable to delete.")).build();

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

        // Sanity check
        if (!binForm.validate())
            return Response.status(400).entity(ImmutableMap.of("message", "Bad bin details.")).build();

        // Set Hibernate and get user and bin
        Session session = HibernateUtil.getSession();

        String user = UserHelper.getCurrentUser(request);

        Bin bin = (Bin) session.get(Bin.class, binId);

        // Check the existence of the bin
        if (bin == null)
            return Response.status(404).build();

        if (!bin.isOwner(user))
            return Response.status(403).entity(ImmutableMap.of("message", "No permissions to modify bin.")).build();

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
                                            @QueryParam("token") String token,
                                            @FormParam("user") String _user) {

        if (_user != null && (newUsers == null || newUsers.length == 0))
            newUsers = new String[] {_user};

        // Set Hibernate and get user and bin
        Session session = HibernateUtil.getSession();

        String user = UserHelper.getCurrentUser(request);

        Bin bin = (Bin) session.get(Bin.class, binId);

        // Check the existence of the bin
        if (bin == null)
            return Response.status(404).build();

        if (!bin.canAddPermission(user, token))
            return Response.status(403).entity(ImmutableMap.of("message", "Cannot add permissions to bin.")).build();

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

    @DELETE
    @Path("/{binId}/permissions/{user}")
    public Object deleteBinAccessPermission(@PathParam("binId") long binId,
                                            @PathParam("user") String user) {

        String[] users = new String[] {user};
        return deleteBinAccessPermissions(binId, users, null);
    }
    /*
    Done
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
            return Response.status(403).entity(ImmutableMap.of("message", "Cannot delete permissions from bin.")).build();

        System.out.println(users.length);

        // Get all BinPermissions
        @SuppressWarnings("unchecked")
        List<BinAccessPermission> permissions = session.createCriteria(BinAccessPermission.class)
                                                       .add(Restrictions.in("userCrsId", users))
                                                       .add(Restrictions.not(Restrictions.eq("userCrsId", bin.getOwner())))
                                                       .add(Restrictions.eq("bin", bin))
                                                       .list();

        // Delete all BinPermissions
        for (BinAccessPermission perm: permissions)
            if (!perm.getUserCrsId().equals(bin.getOwner()))
                session.delete(perm);

        return Response.ok().build();
    }

    /*
    Done
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
            return Response.status(403).entity(ImmutableMap.of("message", "Cannot add permissions to bin.")).build();

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
     */
    @DELETE
    @Path("/{binId}/marking-permissions")
    public Response deleteBinMarkingPermissions(@PathParam("binId") long binId,
                                                @QueryParam("markingUsers[]") String[] markingUsers,
                                                @QueryParam("questionIds[]") Long[] questionIds,
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
            return Response.status(403).entity(ImmutableMap.of("message", "Cannot delete permissions from bin.")).build();

        // Check if marking permissions already exists and skip if it does
        List permissions = session.createCriteria(BinMarkingPermission.class)
                                  .add(Restrictions.eq("bin", bin))
                                  .add(Restrictions.in("userCrsId", markingUsers))
                                  .add(Restrictions.not(Restrictions.eq("userCrsId", bin.getOwner())))
                                  .add(Restrictions.in("questionId", questionIds))
                                  .add(Restrictions.in("questionOwner", questionOwners))
                                  .list();

        // Add the new user permission
        for (int i = 0; i < permissions.size(); i++)
            session.delete(permissions);

        return Response.ok().build();
    }

    /*
    Done
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
            return Response.status(403).entity(ImmutableMap.of("message", "Cannot add question to bin.")).build();

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
            return Response.status(403).entity(ImmutableMap.of("message", "Cannot delete question from bin.")).build();

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
