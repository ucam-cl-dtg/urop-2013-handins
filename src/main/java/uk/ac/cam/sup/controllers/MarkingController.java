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

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.*;

@Path("/marking/bins/{binId}")
public class MarkingController {

    @SuppressWarnings({"UnusedDeclaration"})
    @Context
    private HttpServletRequest request;

    /*
    Done
     */
    @POST
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces("application/json")
    public Object createMarkedSubmission(@MultipartForm FileUploadForm uploadForm,
                                         @PathParam("binId") long binId) {

        // Set Hibernate and get user and bin
        Session session = HibernateUtil.getSession();

        String user = UserHelper.getCurrentUser(request);

        Bin bin = (Bin) session.get(Bin.class, binId);

        // Check the existence of the bin
        if (bin == null)
            return Response.status(404).build();
        if (!bin.canAddMarkedSubmission(user))
            return Response.status(403).entity(ImmutableMap.of("message", "Cannot add marked submission.")).build();

        // New markedSubmission to get Id
        MarkedSubmission markedSubmission = new MarkedSubmission();
        session.save(markedSubmission);

        // Create directory
        String directory = FilesManip.newDirectory("files/" + user + "/submissions/annotated/", false);

        // Save the submission
        String fileName = "submission_" + markedSubmission.getId() + ".pdf";
        try {
            FilesManip.fileSave(uploadForm.file, directory + fileName);
        } catch (Exception e) {
            return Response.status(500).entity(ImmutableMap.of("message", "Could not save file.")).build();
        }

        // Add the submission to the database
        markedSubmission.setFilePath(directory + fileName);
        markedSubmission.setBin(bin);
        markedSubmission.setOwner(user);

        session.update(markedSubmission);

        // Split the markedSubmission into markedAnswers
        try {
            FilesManip.distributeSubmission(user, markedSubmission);
        }
        catch (Exception e) {
            return Response.status(500).entity(ImmutableMap.of("message", "Unable to distribute the submission."));
        }

        return ImmutableMap.of("unmarkedSubmission", ImmutableMap.of("id", markedSubmission.getId()),
                                                                     "bin", bin.getId());
    }

    /*
    Done
     */
    @DELETE
    @Path("/{submissionId}")
    @Produces("application/json")
    public Object deleteMarkedSubmission(@PathParam("binId") long binId,
                                         @PathParam("submissionId") long submissionId) {

        // Set Hibernate and get user and bin
        Session session = HibernateUtil.getSession();

        String user = UserHelper.getCurrentUser(request);

        Bin bin = (Bin) session.get(Bin.class, binId);

        // Check the existence of the bin
        if (bin == null)
            return Response.status(404).build();
        if (!bin.canAddMarkedSubmission(user))
            return Response.status(403).entity(ImmutableMap.of("message", "Cannot delete marked submission.")).build();

        // Get markedSubmission from database and check
        MarkedSubmission markedSubmission = (MarkedSubmission) session.get(MarkedSubmission.class, submissionId);
        if (markedSubmission == null)
            return Response.status(404).build();
        if (!markedSubmission.getOwner().equals(user))
            return Response.status(403).entity(ImmutableMap.of("message", "Cannot delete marked submission.")).build();

        // Delete its markedAnswers
        for (MarkedAnswer markedAnswer : markedSubmission.getAllAnswers()) {
            markedAnswer.getAnswer().setAnnotated(false);

            FilesManip.fileDelete(markedAnswer.getFilePath());

            session.delete(markedAnswer);
        }

        // Delete the markedSubmission
        FilesManip.fileDelete(markedSubmission.getFilePath());
        session.delete(markedSubmission);

        return Response.ok().build();
    }

    /*
    Done
     */
    @GET
    @Path("/students")
    @Produces("application/json")
    public Object viewAllStudentSubmissions(@PathParam("binId") long binId) {

        // Set Hibernate and get user and bin
        Session session = HibernateUtil.getSession();

        String user = UserHelper.getCurrentUser(request);

        Bin bin = (Bin) session.get(Bin.class, binId);

        // Check the existence of the bin
        if (bin == null)
            return Response.status(404).build();

        // Get the list of all students in the bin
        List<BinAccessPermission> allAccess = new LinkedList<BinAccessPermission>(bin.getAccessPermissions());

        // Filter all students
        List<ImmutableMap<String, ?>> studentSubmissions = new LinkedList<ImmutableMap<String, ?>>();
        for (BinAccessPermission permission : allAccess) {
            String student = permission.getUserCrsId();

            // Get all the Answers from the student
                @SuppressWarnings("unchecked")
            List<Answer> answers = session.createCriteria(Answer.class)
                                          .add(Restrictions.eq("bin", bin))
                                          .add(Restrictions.eq("owner", student))
                                          .add(Restrictions.eq("last", true))
                                          .list();

            /*
            Check if the student has an answer visible to the user
            Check if the student is fully marked
             */
            boolean available = false;
            boolean isMarked = true;
            for (Answer answer : answers)
                if (answer.isLast() && bin.canSeeAnswer(user, answer)) {
                    available = true;

                    isMarked &= answer.isAnnotated();
                }

            // Add if visible
            if (available)
                studentSubmissions.add(ImmutableMap.of("student", student, "isMarked", isMarked));
        }

        return ImmutableMap.of("students", studentSubmissions);
    }

    /*
    Done
     */
    @GET
    @Path("students/{studentCrsId}")
    @Produces("application/json")
    public Object viewStudent(@PathParam("binId") long binId,
                              @PathParam("studentCrsId") String studentCrsId) {

        // Set Hibernate and get user and bin
        Session session = HibernateUtil.getSession();

        String user = UserHelper.getCurrentUser(request);

        Bin bin = (Bin) session.get(Bin.class, binId);

        // Check the existence of the bin
        if (bin == null)
            return Response.status(404).build();

        // Get all the questions associated to the bin
        @SuppressWarnings("unchecked")
        List<ProposedQuestion> questions = session.createCriteria(ProposedQuestion.class)
                                                  .add(Restrictions.eq("bin", bin))
                                                  .addOrder(Order.asc("id"))
                                                  .list();

        // And now filter for the student
        List<ImmutableMap<String, ?>> studentQuestions = new LinkedList<ImmutableMap<String, ?>>();
        for (ProposedQuestion question : questions) {

            // Query the database
            @SuppressWarnings("unchecked")
            List<Answer> answers = session.createCriteria(Answer.class)
                                          .add(Restrictions.eq("bin", bin))
                                          .add(Restrictions.eq("owner", studentCrsId))
                                          .add(Restrictions.eq("question", question))
                                          .add(Restrictions.eq("last", true))
                                          .list();

            /*
            If the answer exists then return it if it's visible to the user.
            If the answer does not exist or it's not visible then say it doesn't exist.
             */
            if (answers.size() > 0 && bin.canSeeAnswer(user, answers.get(0)))
                studentQuestions.add(ImmutableMap.of("questionName", question.getName(),
                                                     "questionId", question.getId(),
                                                     "exists", true,
                                                     "isMarked", answers.get(0).isAnnotated()));
            else studentQuestions.add(ImmutableMap.of("questionName", question.getName(),
                                                      "questionId", question.getId(),
                                                      "exists", false,
                                                      "isMarked", false));
        }

        return ImmutableMap.of("studentQuestions", studentQuestions,
                               "student", studentCrsId);
    }

    /*
    Done
     */
    @POST
    @Path("students/{studentCrsId}")
    @Produces("application/json")
    public Object annotateStudent(@PathParam("binId") long binId,
                                  @PathParam("studentCrsId") String studentCrsId) {

        // Set Hibernate and get user and bin
        Session session = HibernateUtil.getSession();

        String user = UserHelper.getCurrentUser(request);

        Bin bin = (Bin) session.get(Bin.class, binId);

        // Check the existence of the bin
        if (bin == null)
            return Response.status(404).build();

        // Get all the questions associated to the bin
        List<ProposedQuestion> questions = new LinkedList<ProposedQuestion>(bin.getQuestionSet());

        // And now filter the questions and get the newState
        List<Answer> answerList = new LinkedList<Answer>();
        boolean newState = true;
        for (ProposedQuestion question : questions) {

            // Query the database
            @SuppressWarnings("unchecked")
            List<Answer> answers = session.createCriteria(Answer.class)
                                          .add(Restrictions.eq("bin", bin))
                                          .add(Restrictions.eq("owner", studentCrsId))
                                          .add(Restrictions.eq("question", question))
                                          .add(Restrictions.eq("last", true))
                                          .list();

            /*
            if it exists and it is visible then change the annotation
            if it doesn't exist then do nothing
             */
            if (answers.size() > 0)
                if (bin.canSeeAnswer(user, answers.get(0))) {
                    answerList.add(answers.get(0));

                    newState &= answers.get(0).isAnnotated();
                }
        }

        // Set the !newState to all the Answers
        for (Answer answer : answerList)
            answer.setAnnotated(!newState);

        return Response.ok().build();
    }

    /*
    Done
     */
    @POST
    @Path("students/{studentCrsId}/questions/{questionId}")
    @Produces("application/json")
    public Object annotateStudentQuestion(@PathParam("binId") long binId,
                                          @PathParam("questionId") long questionId,
                                          @PathParam("studentCrsId") String studentCrsId) {

        return annotateQuestionStudent(binId, questionId, studentCrsId);
    }

    /*
    Done
     */
    @GET
    @Path("/questions")
    @Produces("application/json")
    public Object viewAllQuestionSubmissions(@PathParam("binId") long binId) {

        // Set Hibernate and get user and bin
        Session session = HibernateUtil.getSession();

        String user = UserHelper.getCurrentUser(request);

        Bin bin = (Bin) session.get(Bin.class, binId);

        // Check the existence of the bin
        if (bin == null)
            return Response.status(404).build();

        // Get all the questions associated to the bin
        @SuppressWarnings("unchecked")
        List<ProposedQuestion> questions = session.createCriteria(ProposedQuestion.class)
                                                  .add(Restrictions.eq("bin", bin))
                                                  .addOrder(Order.asc("id"))
                                                  .list();

        // And now filter the questions
        List<ImmutableMap<String, ?>> questionList = new LinkedList<ImmutableMap<String, ?>>();
        for (ProposedQuestion question : questions) {

            // Get the answers
            @SuppressWarnings("unchecked")
            List<Answer> answers = session.createCriteria(Answer.class)
                                          .add(Restrictions.eq("bin", bin))
                                          .add(Restrictions.eq("question.id", question.getId()))
                                          .add(Restrictions.eq("last", true))
                                          .list();

            /*
            Check if the question has an answer visible to the user
            Check if the question is fully marked
             */
            boolean available = false;
            boolean isMarked = true;
            for (Answer answer : answers)
                if (bin.canSeeAnswer(user, answer)) {
                    available = true;

                    isMarked &= answer.isAnnotated();
                }

            // If there is anything available then add it to the question list
            if (available)
                questionList.add(ImmutableMap.of("questionName", question.getName(),
                                                 "questionId", question.getId(),
                                                 "isMarked", isMarked));
        }

        return ImmutableMap.of("questionList", questionList);
    }

    /*
    Done
     */
    @GET
    @Path("/questions/{questionId}")
    @Produces("application/json")
    public Object viewQuestion(@PathParam("binId") long binId,
                               @PathParam("questionId") long questionId) {

        // Set Hibernate and get user and bin
        Session session = HibernateUtil.getSession();

        String user = UserHelper.getCurrentUser(request);

        Bin bin = (Bin) session.get(Bin.class, binId);

        // Check the existence of the bin
        if (bin == null)
            return Response.status(404).build();

        // Get the answers
        @SuppressWarnings("unchecked")
        List<Answer> answers = session.createCriteria(Answer.class)
                                      .add(Restrictions.eq("bin", bin))
                                      .add(Restrictions.eq("question.id", questionId))
                                      .add(Restrictions.eq("last", true))
                                      .list();

        // Filter the answers to get the ones which are visible
        List<ImmutableMap<String, ?>> studentList = new LinkedList<ImmutableMap<String, ?>>();
        for (Answer answer : answers)
            if (bin.canSeeAnswer(user, answer))
                studentList.add(ImmutableMap.of("owner", answer.getOwner(),
                                                "isMarked", answer.isAnnotated()));

        return ImmutableMap.of("studentList", studentList,
                               "question", questionId);
    }

    /*
    Done
     */
    @POST
    @Path("questions/{questionId}")
    @Produces("application/json")
    public Object annotateQuestion(@PathParam("binId") long binId,
                                   @PathParam("questionId") long questionId) {

        // Set Hibernate and get user, bin and question
        Session session = HibernateUtil.getSession();

        String user = UserHelper.getCurrentUser(request);

        Bin bin = (Bin) session.get(Bin.class, binId);

        // Check the existence of the bin
        if (bin == null)
            return Response.status(404).build();

        // Get the answers
        @SuppressWarnings("unchecked")
        List<Answer> answers = session.createCriteria(Answer.class)
                                      .add(Restrictions.eq("bin", bin))
                                      .add(Restrictions.eq("question.id", questionId))
                                      .add(Restrictions.eq("last", true))
                                      .list();

        /*
        And now change the answers
        if it exists and it is visible then change the annotation
        if it doesn't exist then do nothing
         */
        for (Answer answer : answers)
            if (bin.canSeeAnswer(user, answer))
                answer.setAnnotated(!answer.isAnnotated());

        return Response.ok().build();
    }

    /*
    Done
     */
    @POST
    @Path("questions/{questionId}/students/{studentCrsId}")
    @Produces("application/json")
    public Object annotateQuestionStudent(@PathParam("binId") long binId,
                                          @PathParam("questionId") long questionId,
                                          @PathParam("studentCrsId") String studentCrsId) {

        // Set Hibernate and get user, bin and question
        Session session = HibernateUtil.getSession();

        String user = UserHelper.getCurrentUser(request);

        Bin bin = (Bin) session.get(Bin.class, binId);

        // Check the existence of the bin
        if (bin == null)
            return Response.status(404).build();

        // Get the answers
        @SuppressWarnings("unchecked")
        List<Answer> answers = session.createCriteria(Answer.class)
                                      .add(Restrictions.eq("bin", bin))
                                      .add(Restrictions.eq("question.id", questionId))
                                      .add(Restrictions.eq("owner", studentCrsId))
                                      .add(Restrictions.eq("last", true))
                                      .list();

        /*
        And now change the answers
        if it exists and it is visible then change the annotation
        if it doesn't exist then do nothing
         */
        for (Answer answer : answers)
            if (bin.canSeeAnswer(user, answer))
                    answer.setAnnotated(!answer.isAnnotated());

        return Response.ok().build();
    }
}
