package uk.ac.cam.sup.controllers;

import com.google.common.collect.ImmutableMap;
import org.hibernate.Session;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import uk.ac.cam.sup.HibernateUtil;
import uk.ac.cam.sup.helpers.UserHelper;
import uk.ac.cam.sup.models.Answer;
import uk.ac.cam.sup.models.Bin;
import uk.ac.cam.sup.models.UnmarkedSubmission;
import uk.ac.cam.sup.tools.FilesManip;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import java.io.File;
import java.util.*;

@Path ("/submissions")
public class SubmissionController {

    @SuppressWarnings({"UnusedDeclaration"})
    @Context
    private HttpServletRequest request;

    /*
    Done

    Checked
     */
    @GET
    @Path("/{submissionId}")
    @Produces("application/json")
    public Object viewSubmission(@PathParam("submissionId") long submissionId) {

        // Set Hibernate and get user and submission
        Session session = HibernateUtil.getSession();

        String user = UserHelper.getCurrentUser(request);

        UnmarkedSubmission unmarkedSubmission = (UnmarkedSubmission) session.get(UnmarkedSubmission.class, submissionId);

        if (unmarkedSubmission == null)
            return Response.status(404).build();

        // Get Bin and check
        Bin bin = unmarkedSubmission.getBin();

        if (!bin.canSeeSubmission(user, unmarkedSubmission))
            return Response.status(403).entity(ImmutableMap.of("message", "Cannot see submission.")).build();

        List<ImmutableMap<String, String>> answerList = new LinkedList<ImmutableMap<String, String>>();
        for (Object answer : session.createCriteria(Answer.class)
                                    .add(Restrictions.eq("unmarkedSubmission", unmarkedSubmission))
                                    .addOrder(Order.asc("question.id"))
                                    .list())
            answerList.add(ImmutableMap.of("question", ((Answer) answer).getQuestion().getName(), "link", "/submissions/" + submissionId + "/" + ((Answer) answer).getId() + "/download", "bin", Long.toString(bin.getId())));

        return ImmutableMap.of("answers", answerList);
    }

    /*
    Done

    Checked
     */
    @DELETE
    @Path("/{submissionId}")
    @Produces("application/json")
    public Object deleteSubmission(@PathParam("submissionId") long submissionId) {

        // Set Hibernate and get user and submission
        Session session = HibernateUtil.getSession();

        String user = UserHelper.getCurrentUser(request);

        UnmarkedSubmission unmarkedSubmission = (UnmarkedSubmission) session.get(UnmarkedSubmission.class, submissionId);

        if (unmarkedSubmission == null)
            return Response.status(404).build();

        // Get Bin and check
        Bin bin = unmarkedSubmission.getBin();

        if (!bin.canDeleteSubmission(user, unmarkedSubmission))
            return Response.status(403).entity(ImmutableMap.of("message", "Cannot delete submission.")).build();

        // See if there are annotated answers
        for (Answer answer : unmarkedSubmission.getAllAnswers())
            if (answer.isDownloaded())
                return Response.status(403).entity(ImmutableMap.of("message", "Cannot delete submission. Answer to " + answer.getQuestion().getName() + " has been downloaded for marking.")).build();

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

        // Delete the actual submission
        FilesManip.fileDelete(unmarkedSubmission.getFilePath());
        FilesManip.fileDelete(unmarkedSubmission.getOriginalFilePath());
        session.delete(unmarkedSubmission);

        return Response.ok().build();
    }

    /*
    Done

    Checked
     */
    @GET
    @Path("/{submissionId}/download")
    @Produces("application/pdf")
    public Object downloadSubmission(@PathParam("submissionId") long submissionId) {

        // Set Hibernate and get user and submission
        Session session = HibernateUtil.getSession();

        String user = UserHelper.getCurrentUser(request);

        UnmarkedSubmission unmarkedSubmission = (UnmarkedSubmission) session.get(UnmarkedSubmission.class, submissionId);

        if (unmarkedSubmission == null)
            return Response.status(404).build();

        // Get Bin and check
        Bin bin = unmarkedSubmission.getBin();

        if (!bin.canSeeSubmission(user, unmarkedSubmission))
            return Response.status(403).entity(ImmutableMap.of("message", "Cannot see submission.")).build();

        // returning the queried file
        return Response.ok(new File(unmarkedSubmission.getOriginalFilePath())).build();
    }

    /*
    Done

    Checked
     */
    @GET
    @Path("/{submissionId}/{answerId}/download")
    @Produces("application/pdf")
    public Object downloadAnswer(@PathParam("answerId") long answerId) {

        // Set Hibernate and get user and answer
        Session session = HibernateUtil.getSession();

        String user = UserHelper.getCurrentUser(request);

        Answer answer = (Answer) session.get(Answer.class, answerId);

        if (answer == null)
            return Response.status(404).build();

        // Get Bin and check
        Bin bin = answer.getBin();

        if (!bin.canSeeAnswer(user, answer))
            return Response.status(403).entity(ImmutableMap.of("message", "Cannot see answer.")).build();

        // returning the queried file
        return Response.ok(new File(answer.getFilePath())).build();
    }
}
