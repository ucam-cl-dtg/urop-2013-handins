package uk.ac.cam.sup.controllers;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import javax.ws.rs.DELETE;
import javax.ws.rs.FormParam;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;

import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;
import org.jboss.resteasy.annotations.Form;

import uk.ac.cam.cl.dtg.ldap.LDAPObjectNotFoundException;
import uk.ac.cam.cl.dtg.teaching.api.DashboardApi;
import uk.ac.cam.cl.dtg.teaching.api.QuestionsApi.Question;
import uk.ac.cam.cl.dtg.teaching.api.QuestionsApi.QuestionSet;
import uk.ac.cam.cl.dtg.teaching.api.QuestionsApi.QuestionsApiWrapper;
import uk.ac.cam.cl.dtg.teaching.hibernate.HibernateUtil;
import uk.ac.cam.sup.forms.BinForm;
import uk.ac.cam.sup.models.Bin;
import uk.ac.cam.sup.models.BinAccessPermission;
import uk.ac.cam.sup.models.BinMarkingPermission;
import uk.ac.cam.sup.models.ProposedQuestion;

import com.google.common.collect.ImmutableMap;

// Documented

@Path("/bins")
@Produces("application/json")
public class BinEditor extends ApplicationController {


    /*
    Done

    Checked
     */
    @DELETE
    @Path("/{binId}")
    public Response deleteBin(@PathParam("binId") long binId,
                              @QueryParam("token") String token) {

        // Set Hibernate and get user and bin
        Session session = HibernateUtil.getInstance().getSession();

        String user = getCurrentUser();

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

        return Response.status(204).build();
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
        Session session = HibernateUtil.getInstance().getSession();

        String user = getCurrentUser();

        Bin bin = (Bin) session.get(Bin.class, binId);

        // Check the existence of the bin
        if (bin == null)
            return Response.status(404).build();

        if (!bin.hasTotalAccess(user))
            return Response.status(403).entity(ImmutableMap.of("message", "No permissions to modify bin.")).build();

        // Update the bin accordingly
        binForm.save(bin);
        session.update(bin);

        return Response.status(204).build();
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
        Session session = HibernateUtil.getInstance().getSession();

        String user = getCurrentUser();

        Bin bin = (Bin) session.get(Bin.class, binId);

        // Check the existence of the bin
        if (bin == null)
            return Response.status(404).build();

        if (!bin.canAddPermission(user, token))
            return Response.status(403).entity(ImmutableMap.of("message", "Cannot add permissions to bin.")).build();

        // Get all existing users
        Set<String> usersWithPermission = new TreeSet<>();
        for (BinAccessPermission perm: bin.getAccessPermissions())
            usersWithPermission.add(perm.getUserCrsId());

        // Add the new users
        for (String newUser : newUsers) {
            newUser = newUser.trim();

            if (!newUser.isEmpty() && !usersWithPermission.contains(newUser))
                try {
                    session.save(new BinAccessPermission(bin, newUser, new DashboardApi.DashboardApiWrapper(getDashboardUrl(), getApiKey())));
                } catch (LDAPObjectNotFoundException e) {
                    Response.status(202).build();
                }
        }

        return Response.status(204).build();
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
        Session session = HibernateUtil.getInstance().getSession();

        String user = getCurrentUser();

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

        return Response.status(204).build();
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
        Session session = HibernateUtil.getInstance().getSession();

        String user = getCurrentUser();

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

        return Response.status(204).build();
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
        Session session = HibernateUtil.getInstance().getSession();

        String user = getCurrentUser();

        Bin bin = (Bin) session.get(Bin.class, binId);

        // Check the existence of the bin
        if (bin == null)
            return Response.status(404).build();

        if (!bin.canDeletePermission(user, token))
            return Response.status(403).entity(ImmutableMap.of("message", "Cannot delete permissions from bin.")).build();

        // Check if marking permissions already exists and skip if it does
        List<?> permissions = session.createCriteria(BinMarkingPermission.class)
                                  .add(Restrictions.eq("bin", bin))
                                  .add(Restrictions.in("userCrsId", markingUsers))
                                  .add(Restrictions.not(Restrictions.eq("userCrsId", bin.getOwner())))
                                  .add(Restrictions.in("questionId", questionIds))
                                  .add(Restrictions.in("questionOwner", questionOwners))
                                  .list();

        // Add the new user permission
        for (int i = 0; i < permissions.size(); i++)
            session.delete(permissions);

        return Response.status(204).build();
    }

    @POST
    @Path("/{binId}/questions/import")
    public Object importQuestionSet(@PathParam("binId") long binId,
                                    @FormParam("questionSetId") long questionSetId) {

        QuestionsApiWrapper api = new QuestionsApiWrapper(getQuestionsUrl(), getApiKey());

        // TODO: Fix this when questions support global admin keys

        QuestionSet set = api.getQuestionSet(questionSetId, getCurrentUser());

        String[] names = new String[set.getQuestions().size()],
                 links = new String[set.getQuestions().size()];

        int index = 0;
        for (Question question: set.getQuestions()) {
            names[index] = question.getTitle();
            links[index] = "/questions/q/" + question.getId();
            index ++;
        }


        return addBinQuestions(binId, names, links, null, null);
    }

    /*
    Done
     */
    @POST
    @Path("/{binId}/questions")
    public Object addBinQuestions(@PathParam("binId") long binId,
                                  @FormParam("name[]") String[] newQuestionNames,
                                  @FormParam("link[]") String[] newLinks,
                                  @FormParam("name") String questionName,
                                  @FormParam("link") String link) {
        boolean singleItem = false;
        if (questionName != null && (newQuestionNames == null || newQuestionNames.length == 0)) {
            newQuestionNames = new String[] {questionName};
            newLinks = new String[] {link};
            singleItem = true;
        }

        // Set Hibernate and get user
        Session session = HibernateUtil.getInstance().getSession();

        String user = getCurrentUser();

        Bin bin = (Bin) session.get(Bin.class, binId);

        // Check the existence of the bin
        if (bin == null)
            return Response.status(404).build();

        if (!bin.hasTotalAccess(user))
            return Response.status(403).entity(ImmutableMap.of("message", "Cannot add question to bin.")).build();

        // Get all existing questions
        Set<String> existingQuestions = new TreeSet<>();
        for (ProposedQuestion question : bin.getQuestionSet())
            existingQuestions.add(question.getName());

        int newQuestions = 0;
        ProposedQuestion lastQuestion = null;
        // Add the new questions
        for (int i = 0; i < newQuestionNames.length; i++) {
            String newQuestion = newQuestionNames[i], newLink = newLinks[i];
            newQuestion = newQuestion.trim();

            if (!newQuestion.isEmpty() && !existingQuestions.contains(newQuestion)) {
                ProposedQuestion question = new ProposedQuestion();
                session.save(question);

                // Add the question details to the database
                question.setName(newQuestion);
                question.setLink(newLink);
                question.setBin(bin);

                session.update(question);
                lastQuestion = question;
                newQuestions ++;
            }
        }

        if (singleItem && newQuestions == 0)
            return Response.status(400).entity(ImmutableMap.of("message", "New question name is invalid")).build();
        if (singleItem) {
            return ImmutableMap.of("id", lastQuestion.getId());
        }
        return Response.status(204).build();
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
        Session session = HibernateUtil.getInstance().getSession();

        String user = getCurrentUser();

        Bin bin = (Bin) session.get(Bin.class, binId);

        // Check the existence of the bin
        if (bin == null)
            return Response.status(404).build();

        if (!bin.hasTotalAccess(user))
            return Response.status(403).entity(ImmutableMap.of("message", "Cannot delete question from bin.")).build();

        // Get the question and delete it or add it to the list of undeletable
        List<ImmutableMap<String,?>> unDel = new LinkedList<ImmutableMap<String,?>>();
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
        return Response.status(204).build();
    }
}
