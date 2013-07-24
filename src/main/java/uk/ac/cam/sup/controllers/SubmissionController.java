package uk.ac.cam.sup.controllers;

import com.google.common.collect.ImmutableMap;
import org.hibernate.Session;
import uk.ac.cam.sup.HibernateUtil;
import uk.ac.cam.sup.helpers.UserHelper;
import uk.ac.cam.sup.models.Answer;
import uk.ac.cam.sup.models.Bin;
import uk.ac.cam.sup.models.UnmarkedSubmission;
import uk.ac.cam.sup.tools.FilesManip;

import javax.ws.rs.*;
import javax.ws.rs.core.Response;
import java.io.File;
import java.util.*;

@Path ("/submission")
public class SubmissionController {

    @GET
    @Path("/{submissionId}")
    @Produces("application/json")
    public Object viewSubmission(@PathParam("submissionId") long submissionId) {

        // Set Hibernate and get user
        Session session = HibernateUtil.getSession();

        String user = UserHelper.getCurrentUser();

        UnmarkedSubmission unmarkedSubmission = (UnmarkedSubmission) session.get(UnmarkedSubmission.class, submissionId);

        if (unmarkedSubmission == null)
            return Response.status(404).build();

        // Get Bin and check
        Bin bin = unmarkedSubmission.getBin();

        if (!bin.canSeeSubmission(user, unmarkedSubmission))
            return Response.status(401).build();

        List<ImmutableMap<String, String>> answerList = new LinkedList<ImmutableMap<String, String>>();
        for (Answer answer : unmarkedSubmission.getAllAnswers())
            answerList.add(ImmutableMap.of("question", answer.getQuestion().getName(), "link", "submission/" + answer.getId() + "/download", "bin", Long.toString(bin.getId())));

        return ImmutableMap.of("answers", answerList);
    }

    @GET
    @Path("/{submissionId}/{answerId}/download")
    @Produces("application/pdf")
    public Object downloadAnswer(@PathParam("answerId") long answerId) {

        // Set Hibernate and get user
        Session session = HibernateUtil.getSession();

        String user = UserHelper.getCurrentUser();

        Answer answer = (Answer) session.get(Answer.class, answerId);

        if (answer == null)
            return Response.status(404).build();

        // Get Bin and check
        Bin bin = answer.getBin();

        if (!bin.canSeeAnswer(user, answer))
            return Response.status(401).build();

        return Response.ok(new File(answer.getFilePath())).build();
    }

    @GET
    @Path("/{submissionId}/download")
    @Produces("application/pdf")
    public Object downloadSubmission(@PathParam("submissionId") long submissionId) {

        // Set Hibernate and get user
        Session session = HibernateUtil.getSession();

        String user = UserHelper.getCurrentUser();

        UnmarkedSubmission unmarkedSubmission = (UnmarkedSubmission) session.get(UnmarkedSubmission.class, submissionId);

        if (unmarkedSubmission == null)
            return Response.status(404).build();

        // Get Bin and check
        Bin bin = unmarkedSubmission.getBin();

        if (!bin.canSeeSubmission(user, unmarkedSubmission))
            return Response.status(401).build();

        return Response.ok(new File(unmarkedSubmission.getFilePath())).build();
    }

    @DELETE
    @Path("/{submissionId}")
    @Produces("application/json")
    public Object deleteSubmission(@PathParam("submissionId") long submissionId) {

        // Set Hibernate and get user
        Session session = HibernateUtil.getSession();

        String user = UserHelper.getCurrentUser();

        UnmarkedSubmission unmarkedSubmission = (UnmarkedSubmission) session.get(UnmarkedSubmission.class, submissionId);

        if (unmarkedSubmission == null)
            return Response.status(404).build();

        // Get Bin and check
        Bin bin = unmarkedSubmission.getBin();

        if (!bin.canDeleteSubmission(user, unmarkedSubmission))
            return Response.status(401).build();

        boolean canDelete = true;

        for (Answer answer : unmarkedSubmission.getAllAnswers())
            if (answer.isAnnotated())
                canDelete = false;

        if (!canDelete)
            return Response.status(401).build();

        for (Answer answer : unmarkedSubmission.getAllAnswers()) {
            FilesManip.fileDelete(answer.getFilePath());
            session.delete(answer);
        }

        FilesManip.fileDelete(unmarkedSubmission.getFilePath());
        session.delete(unmarkedSubmission);

        return Response.status(200).build();
    }
}
