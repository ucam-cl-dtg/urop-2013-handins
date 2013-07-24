package uk.ac.cam.sup.controllers;

import com.google.common.collect.ImmutableMap;
import com.itextpdf.text.DocumentException;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;
import org.jboss.resteasy.annotations.providers.multipart.MultipartForm;
import uk.ac.cam.sup.HibernateUtil;
import uk.ac.cam.sup.exceptions.MetadataNotFoundException;
import uk.ac.cam.sup.forms.FileUploadForm;
import uk.ac.cam.sup.helpers.UserHelper;
import uk.ac.cam.sup.models.*;
import uk.ac.cam.sup.structures.Marking;
import uk.ac.cam.sup.tools.FilesManip;
import uk.ac.cam.sup.tools.PDFManip;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.File;
import java.io.IOException;
import java.util.*;

@Path("/marking")
public class MarkingController {

    /*
    Done?
     */
    @POST
    @Path("/bin/{binId}")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces("application/json")
    public Object createMarkedSubmission(@MultipartForm FileUploadForm uploadForm,
                                         @PathParam("binId") long binId) throws IOException, MetadataNotFoundException, DocumentException {

        // Set Hibernate and get user
        Session session = HibernateUtil.getSession();

        String user = UserHelper.getCurrentUser();

        // Get Bin and check
        Bin bin = BinController.getBin(binId);

        if (bin == null)
            return Response.status(401).build();
        if (!bin.canAddMarkedSubmission(user))
            return Response.status(401).build();

        // New submission and get id
        MarkedSubmission markedSubmission = new MarkedSubmission();
        session.save(markedSubmission);

        // Create directory
        String directory = "temp/" + user + "/submissions/annotated/";
        File fileDirectory = new File(directory);
        boolean ok = fileDirectory.mkdirs();

        String fileName = "submission_" + markedSubmission.getId() + ".pdf";

        FilesManip.fileSave(uploadForm.file, directory + fileName);

        markedSubmission.setFilePath(directory + fileName);

        session.update(markedSubmission);

        List<String> listOfUploads = null;
        FilesManip.distributeSubmission(markedSubmission);

        return ImmutableMap.of("id", markedSubmission.getId(), "List of Student/Question", listOfUploads);
    }

    @DELETE
    @Path("bin/{binId}/{submissionId}")
    @Produces("application/json")
    public Object deleteSubmission(@PathParam("binId") long binId,
                                   @PathParam("submissionId") long submissionId) {

        // Set Hibernate and get user
        Session session = HibernateUtil.getSession();

        String user = UserHelper.getCurrentUser();

        MarkedSubmission markedSubmission = (MarkedSubmission) session.get(MarkedSubmission.class, submissionId);

        if (markedSubmission == null)
            return Response.status(404).build();

        // Get Bin and check
        Bin bin = markedSubmission.getBin();

        if (!bin.canDeleteSubmission(user, markedSubmission))
            return Response.status(401).build();

        for (MarkedAnswer markedAnswer : markedSubmission.getAllAnswers()) {
            FilesManip.fileDelete(markedAnswer.getFilePath());

            session.delete(markedAnswer);
        }

        FilesManip.fileDelete(markedSubmission.getFilePath());
        session.delete(markedSubmission);

        return Response.status(200).build();
    }

    /*
    Done
     */
    @GET
    @Path("/bin/{binId}/student")
    @Produces("application/json")
    public Object viewAllStudentSubmissions(@PathParam("binId") long binId) {

        // Set Hibernate and get user
        Session session = HibernateUtil.getSession();

        String user = UserHelper.getCurrentUser();

        // Get Bin and check
        Bin bin = BinController.getBin(binId);

        if (bin == null)
            return Response.status(401).build();

        List<BinPermission> allAccess = new LinkedList<BinPermission>(bin.getPermissions());
        List<ImmutableMap<String, ?>> students = new LinkedList<ImmutableMap<String, ?>>();

        for (BinPermission permission : allAccess) {
            String student = permission.getUser();
            boolean isMarked = true;

            List<Answer> answers = session.createCriteria(Answer.class)
                                          .add(Restrictions.eq("bin", bin))
                                          .add(Restrictions.eq("owner", student))
                                          .list();

            for (Answer answer : answers)
                if (answer.getMarkedAnswers().size() == 0)
                    isMarked = false;

            boolean available = false;
            for (Answer answer : answers)
                if (bin.canSeeAnswer(user, answer))
                    available = true;

            if (available)
                students.add(ImmutableMap.of("student", student, "isMarked", isMarked));
        }

        return ImmutableMap.of("students", students);
    }

    /*
    Done
     */
    @GET
    @Path("bin/{binId}/student/{studentCrsId}")
    @Produces("application/json")
    public Object viewStudent(@PathParam("binId") long binId,
                              @PathParam("studentCrsId") String studentCrsId) {

        // Set Hibernate and get user
        Session session = HibernateUtil.getSession();

        String user = UserHelper.getCurrentUser();

        // Get Bin and check
        Bin bin = BinController.getBin(binId);

        if (bin == null)
            return Response.status(401).build();

        List<ProposedQuestion> questions = new LinkedList<ProposedQuestion>(bin.getQuestionSet());
        List<ImmutableMap<String, ?>> studentQuestions = new LinkedList<ImmutableMap<String, ?>>();

        for (ProposedQuestion question : questions) {
            List<Answer> answers = session.createCriteria(Answer.class)
                                          .add(Restrictions.eq("bin", bin))
                                          .add(Restrictions.eq("owner", studentCrsId))
                                          .add(Restrictions.eq("question", question)).list();

            boolean exists = answers.size() > 0;

            if (exists)
                if (bin.canSeeAnswer(user, answers.get(0))) {
                    boolean isMarked = answers.get(0).getMarkedAnswers().size() > 0;

                    studentQuestions.add(ImmutableMap.of("questionName", question.getName(),
                                                         "questionId", question.getId(),
                                                         "exists", exists,
                                                         "isMarked", isMarked));
                }
        }

        return ImmutableMap.of("studentQuestions", studentQuestions, "student", studentCrsId);
    }

    /*
    Done
     */
    @GET
    @Path("/bin/{binId}/student/{studentCrsId}/download")
    @Produces("application/pdf")
    public Object getStudent(@PathParam("binId") long binId,
                             @PathParam("studentCrsId") String studentCrsId) throws IOException, DocumentException {

        // Get user
        String user = UserHelper.getCurrentUser();

        // Get Bin and check
        Bin bin = BinController.getBin(binId);

        if (bin == null)
            return Response.status(404).build();

        List<Answer> answers = new LinkedList<Answer>(bin.getAnswers());

        List<Marking> markingList = new LinkedList<Marking>();
        int actualPage = 1;
        for (Answer answer : answers)
            if (answer.getOwner().equals(studentCrsId) && bin.canSeeAnswer(user, answer))
            {
                Marking marking = new Marking();

                marking.setFilePath(answer.getFilePath());
                marking.setFirst(actualPage);

                actualPage += new PDFManip(answer.getFilePath()).getPageCount();
                marking.setLast(actualPage - 1);
                marking.setOwner(studentCrsId);
                marking.setQuestion(answer.getQuestion());
            }

        return FilesManip.resultingFile(markingList);
    }

    /*
    Done
     */
    @GET
    @Path("/bin/{binId}/question")
    @Produces("application/json")
    public Object viewAllQuestionSubmissions(@PathParam("binId") long binId) {

        // Get user
        String user = UserHelper.getCurrentUser();

        // Get Bin and check
        Bin bin = BinController.getBin(binId);

        if (bin == null)
            return Response.status(401).build();

        List<ProposedQuestion> questions = new LinkedList<ProposedQuestion>(bin.getQuestionSet());
        List<ImmutableMap<String, ?>> questionList = new LinkedList<ImmutableMap<String, ?>>();

        for (ProposedQuestion question : questions) {
            List <Answer> answers = new LinkedList<Answer>(question.getAnswers());

            boolean available = false;
            boolean isMarked = true;
            for (Answer answer : answers) {
                if (bin.canSeeAnswer(user, answer))
                    available = true;

                if (!(answer.getMarkedAnswers().size() > 0))
                    isMarked = false;
            }

            if (available)
                questionList.add(ImmutableMap.of("questionName", question.getName(), "questionId", question.getId(), "isMarked", isMarked));
        }

        return ImmutableMap.of("questionList", questionList);
    }

    /*
    Done
     */
    @GET
    @Path("/bin/{binId}/question/{questionId}")
    @Produces("application/json")
    public Object viewQuestion(@PathParam("binId") long binId,
                               @PathParam("questionId") long questionId) {

        // Set Hibernate and get user
        Session session = HibernateUtil.getSession();

        String user = UserHelper.getCurrentUser();

        // Get Bin and check
        Bin bin = BinController.getBin(binId);

        if (bin == null)
            return Response.status(401).build();

        ProposedQuestion question = (ProposedQuestion) session.get(ProposedQuestion.class, questionId);

        List<Answer> answers = new LinkedList<Answer>(question.getAnswers());
        List<ImmutableMap<String, ?>> studentList = new LinkedList<ImmutableMap<String, ?>>();

        for (Answer answer : answers)
            if (bin.canSeeAnswer(user, answer))
                studentList.add(ImmutableMap.of("owner", answer.getOwner(), "isMarked", answer.getMarkedAnswers().size() > 0));

        return ImmutableMap.of("studentList", studentList, "question", questionId);
    }

    @GET
    @Path("/bin/{binId}/question/{questionId}/download")
    @Produces("application/pdf")
    public Object getQuestion(@PathParam("binId") long binId, @PathParam("questionId") long questionId) throws IOException, DocumentException {

        // Set Hibernate and get user
        Session session = HibernateUtil.getSession();

        String user = UserHelper.getCurrentUser();

        // Get Bin and check
        Bin bin = BinController.getBin(binId);

        if (bin == null)
            return Response.status(401).build();

        List<Answer> answers = session.createCriteria(Answer.class)
                                      .add(Restrictions.eq("bin", bin))
                                      .add(Restrictions.eq("question", session.get(ProposedQuestion.class, questionId)))
                                      .list();

        List<Marking> markingList = new LinkedList<Marking>();
        int actualPage = 1;
        for (Answer answer : answers)
            if (bin.canSeeAnswer(user, answer))
            {
                Marking marking = new Marking();

                marking.setFilePath(answer.getFilePath());
                marking.setFirst(actualPage);

                actualPage += new PDFManip(answer.getFilePath()).getPageCount();
                marking.setLast(actualPage - 1);
                marking.setOwner(answer.getOwner());
                marking.setQuestion(answer.getQuestion());
            }

        return FilesManip.resultingFile(markingList);
    }

    @GET
    @Path("/bin/{binId}/student/{studentCrsId}/question/{questionId}")
    @Produces("application/pdf")
    public Object getStudentQuestion(@PathParam("binId") long binId,
                                      @PathParam("studentCrsId") String studentCrsId,
                                      @PathParam("questionId}") long questionId) throws IOException, DocumentException {

        // Set Hibernate and get user
        Session session = HibernateUtil.getSession();

        String user = UserHelper.getCurrentUser();

        // Get Bin and check
        Bin bin = BinController.getBin(binId);

        if (bin == null)
            return Response.status(401).build();

        if (session.createCriteria(Answer.class)
                   .add(Restrictions.eq("bin", bin))
                   .add(Restrictions.eq("question", session.get(ProposedQuestion.class, questionId)))
                   .add(Restrictions.eq("owner", studentCrsId))
                   .list().size() > 0) {
            Answer answer = (Answer) session.createCriteria(Answer.class)
                                        .add(Restrictions.eq("bin", bin))
                                        .add(Restrictions.eq("question", session.get(ProposedQuestion.class, questionId)))
                                        .add(Restrictions.eq("owner", studentCrsId))
                                        .list().get(0);

            if (bin.canSeeAnswer(user, answer))
                return Response.status(401).build();

            List<Marking> markingList = new LinkedList<Marking>();
            Marking marking = new Marking();

            marking.setFilePath(answer.getFilePath());
            marking.setFirst(1);

            marking.setLast(new PDFManip(answer.getFilePath()).getPageCount());
            marking.setOwner(answer.getOwner());
            marking.setQuestion(answer.getQuestion());

            return FilesManip.resultingFile(markingList);
        }

        return Response.status(404).build();
    }

    /*
    Done
     */
    @GET
    @Path("/bin/{binId}/question/{questionId}/student/{studentCrsId}")
    @Produces("application/pdf")
    public Object getQuestionStudent(@PathParam("binId") long binId,
                                      @PathParam("questionId") long questionId,
                                      @PathParam("studentCrsId") String studentCrsId) throws IOException, DocumentException {

        return getStudentQuestion(binId, studentCrsId, questionId);
    }


    /*
    Done
     */
    @GET
    @Path("/bin/{binId}/all")
    @Produces("application/pdf")
    public Object viewAll(@PathParam("binId") long binId) throws IOException, DocumentException {

        // Set Hibernate and get user
        Session session = HibernateUtil.getSession();

        String user = UserHelper.getCurrentUser();

        // Get Bin and check
        Bin bin = BinController.getBin(binId);

        if (bin == null)
            return Response.status(401).build();

        List<Answer> answers = session.createCriteria(Answer.class)
                                      .add(Restrictions.eq("bin", bin)).list();

        List<Marking> markingList = new LinkedList<Marking>();
        int actualPage = 1;
        for (Answer answer : answers)
            if (bin.canSeeAnswer(user, answer))
            {
                Marking marking = new Marking();

                marking.setFilePath(answer.getFilePath());
                marking.setFirst(actualPage);

                actualPage += new PDFManip(answer.getFilePath()).getPageCount();
                marking.setLast(actualPage - 1);
                marking.setOwner(answer.getOwner());
                marking.setQuestion(answer.getQuestion());
            }

        return FilesManip.resultingFile(markingList);
    }
}
