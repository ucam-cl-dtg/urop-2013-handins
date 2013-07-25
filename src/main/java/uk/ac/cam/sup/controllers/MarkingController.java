package uk.ac.cam.sup.controllers;

import com.google.common.collect.ImmutableMap;
import org.hibernate.Session;
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
import java.io.File;
import java.util.*;

@Path("/marking/bin/{binId}")
public class MarkingController {
    @Context
    private HttpServletRequest request;

    /*
    Done

    Checked
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
            return Response.status(401).build();

        // New markedSubmission to get Id
        MarkedSubmission markedSubmission = new MarkedSubmission();
        session.save(markedSubmission);

        // Create directory
        String directory = "temp/" + user + "/submissions/annotated/";
        File fileDirectory = new File(directory);
        //noinspection ResultOfMethodCallIgnored
        fileDirectory.mkdirs();

        // Save the submission
        String fileName = "submission_" + markedSubmission.getId() + ".pdf";
        try {
            FilesManip.fileSave(uploadForm.file, directory + fileName);
        } catch (Exception e) {
            return Response.status(345).build();
        }

        // Add the submission to the database
        markedSubmission.setFilePath(directory + fileName);
        markedSubmission.setBin(bin);
        markedSubmission.setOwner(user);

        session.update(markedSubmission);

        // Split the markedSubmission into markedAnswers
        FilesManip.distributeSubmission(user, markedSubmission);

        return ImmutableMap.of("unmarkedSubmission", ImmutableMap.of("id", markedSubmission.getId()),
                                                                     "bin", bin.getId());
    }


    /*
    Done

    Checked
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
            return Response.status(401).build();

        // Get markedSubmission from database and check
        MarkedSubmission markedSubmission = (MarkedSubmission) session.get(MarkedSubmission.class, submissionId);
        if (markedSubmission == null)
            return Response.status(404).build();
        if (!markedSubmission.getOwner().equals(user))
            return Response.status(401).build();

        // Delete its markedAnswers
        for (MarkedAnswer markedAnswer : markedSubmission.getAllAnswers()) {
            FilesManip.fileDelete(markedAnswer.getFilePath());

            session.delete(markedAnswer);

            // ToDo: refresh the annotation of their answers
        }

        // Delete the markedSubmission
        FilesManip.fileDelete(markedSubmission.getFilePath());
        session.delete(markedSubmission);

        return Response.ok().build();
    }

    /*
    Done

    Checked
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
        List<BinPermission> allAccess = new LinkedList<BinPermission>(bin.getPermissions());

        // Filter all students
        List<ImmutableMap<String, ?>> studentSubmissions = new LinkedList<ImmutableMap<String, ?>>();
        for (BinPermission permission : allAccess) {
            String student = permission.getUser();

            // Get all the Answers from the student
            @SuppressWarnings("unchecked")
            List<Answer> answers = session.createCriteria(Answer.class)
                                          .add(Restrictions.eq("bin", bin))
                                          .add(Restrictions.eq("owner", student))
                                          .list();

            /*
            Check if the student has an answer visible to the user
            Check if the student is fully marked
             */
            boolean available = false;
            boolean isMarked = true;
            for (Answer answer : answers) {
                if (bin.canSeeAnswer(user, answer))
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

    Checked
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
        List<ProposedQuestion> questions = new LinkedList<ProposedQuestion>(bin.getQuestionSet());

        // And now filter for the student
        List<ImmutableMap<String, ?>> studentQuestions = new LinkedList<ImmutableMap<String, ?>>();
        for (ProposedQuestion question : questions) {

            // Query the database
            @SuppressWarnings("unchecked")
            List<Answer> answers = session.createCriteria(Answer.class)
                                          .add(Restrictions.eq("bin", bin))
                                          .add(Restrictions.eq("owner", studentCrsId))
                                          .add(Restrictions.eq("question", question)).list();

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

    Checked
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

        // And now filter the questions
        for (ProposedQuestion question : questions) {

            // Query the database
            @SuppressWarnings("unchecked")
            List<Answer> answers = session.createCriteria(Answer.class)
                                          .add(Restrictions.eq("bin", bin))
                                          .add(Restrictions.eq("owner", studentCrsId))
                                          .add(Restrictions.eq("question", question)).list();

            /*
            if it exists and it is visible then change the annotation
            if it is no visible then return 401
            if it doesn't exist then do nothing
             */
            if (answers.size() > 0)
                if (bin.canSeeAnswer(user, answers.get(0)))
                    answers.get(0).setAnnotated(!answers.get(0).isAnnotated());
                else return Response.status(401).build();
        }

        return Response.ok().build();
    }

    /*
    Done

    Checked
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
        List<ProposedQuestion> questions = new LinkedList<ProposedQuestion>(bin.getQuestionSet());

        // And now filter the questions
        List<ImmutableMap<String, ?>> questionList = new LinkedList<ImmutableMap<String, ?>>();
        for (ProposedQuestion question : questions) {

            List <Answer> answers = new LinkedList<Answer>(question.getAnswers());

            /*
            Check if the question has an answer visible to the user
            Check if the question is fully marked
             */
            boolean available = false;
            boolean isMarked = true;
            for (Answer answer : answers) {
                if (bin.canSeeAnswer(user, answer))
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

    Checked
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

        // Get the list of answers to the specific
        List<Answer> answers = new LinkedList<Answer>(((ProposedQuestion) session.get(ProposedQuestion.class, questionId)).getAnswers());

        // Filter the answers to get the ones which are visible
        List<ImmutableMap<String, ?>> studentList = new LinkedList<ImmutableMap<String, ?>>();
        for (Answer answer : answers)
            if (bin.canSeeAnswer(user, answer))
                studentList.add(ImmutableMap.of("owner", answer.getOwner(),
                                                "isMarked", answer.isAnnotated()));

        return ImmutableMap.of("studentList", studentList,
                               "question", questionId);
    }  /*
    Done

    Checked
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

        ProposedQuestion proposedQuestion = (ProposedQuestion) session.get(ProposedQuestion.class, questionId);

        // Get the answers
        List<Answer> answers = new LinkedList<Answer>(proposedQuestion.getAnswers());

        /*
        And now change the answers
        if it exists and it is visible then change the annotation
        if it is no visible then return 401
        if it doesn't exist then do nothing
         */
        for (Answer answer : answers)
            if (bin.canSeeAnswer(user, answers.get(0)))
                answer.setAnnotated(!answer.isAnnotated());
            else return Response.status(401).build();

        return Response.ok().build();
    }
}
