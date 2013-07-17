package uk.ac.cam.sup.controllers;

import com.google.common.collect.ImmutableMap;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;
import org.jboss.resteasy.annotations.providers.multipart.MultipartForm;
import uk.ac.cam.sup.HibernateUtil;
import uk.ac.cam.sup.forms.FileUploadForm;
import uk.ac.cam.sup.helpers.UserHelper;
import uk.ac.cam.sup.models.*;
import uk.ac.cam.sup.structures.AnsweredQuestion;
import uk.ac.cam.sup.structures.StudentSubmission;
import uk.ac.cam.sup.tools.FilesManip;
import uk.ac.cam.sup.tools.PDFManip;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.File;
import java.io.IOException;
import java.util.*;

@Path("/marking/{binId}")
@Produces("application/json")
public class MarkingController {

    @POST
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public Object createMarkedSubmission(@MultipartForm FileUploadForm uploadForm, @PathParam("binId") long binId) {

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

        session.getTransaction().commit();

        // Restart session
        session = HibernateUtil.getSession();
        session.beginTransaction();

        String directory = "temp/" + user + "/submissions/annotated/";
        String fileName = "submission_" + markedSubmission.getId() + ".pdf";

        try {
            FilesManip.fileSave(uploadForm.file, directory + fileName);
        } catch (IOException e) {
            e.printStackTrace();

            return Response.status(500).build();
        }

        markedSubmission.setFilePath(directory + fileName);

        session.update(markedSubmission);

        List<String> listOfUploads = null;
        FilesManip.distributeMarkedSubmission(markedSubmission);

        return ImmutableMap.of("id", markedSubmission.getId(), "filePath", markedSubmission.getFilePath(), "User/Question List", listOfUploads);
    }

    @GET
    public Object viewAllSubmissions(@PathParam("binId") long binId) {

        // Get user
        String user = UserHelper.getCurrentUser();

        // Get Bin and check
        Bin bin = BinController.getBin(binId);

        if (bin == null)
            return Response.status(401).build();

        List<Answer> allAnswers = new LinkedList<Answer>(bin.getAnswers());

        Map<String, List<AnsweredQuestion>> studentQuestions = new HashMap<String, List<AnsweredQuestion>>();
        Set<String> studentSet = new TreeSet<String>();

        for (Answer answer : allAnswers) {

            if (bin.canSeeAnswer(user, answer))
            {
                if (!studentSet.contains(answer.getOwner())) {
                    studentQuestions.put(answer.getOwner(), new LinkedList<AnsweredQuestion>());

                    studentSet.add(answer.getOwner());
                }

                boolean marked = answer.getMarkedAnswers().size() > 0;

                AnsweredQuestion q = new AnsweredQuestion(answer.getQuestion(), answer.getFilePath(), true, marked);

                studentQuestions.get(answer.getOwner()).add(q);
            }
        }

        List<StudentSubmission> studentSubmissionList = new LinkedList<StudentSubmission>();

        for (String s : studentSet) {
            StudentSubmission studentSubmission = new StudentSubmission();

            studentSubmission.setName(s);
            studentSubmission.setAnsweredQuestions(studentQuestions.get(s));
            studentSubmission.setMarked(studentSubmission.getAnsweredQuestions().size() == bin.getQuestionCount());

            studentSubmissionList.add(studentSubmission);
        }

        return ImmutableMap.of("studentSubmissionList", studentSubmissionList);
    }

    @GET
    @Path("/getStudent/{studentCrsId}")
    public Object viewStudent(@PathParam("binId") long binId, @PathParam("studentCrsId") String studentCrsId) {

        // Set Hibernate and get user
        Session session = HibernateUtil.getSession();

        // Get Bin and check
        Bin bin = BinController.getBin(binId);

        if (bin == null)
            return Response.status(404).build();

        List<Answer> answerList = new LinkedList<Answer>(bin.getAnswers());

        List<String> questionPathList = new LinkedList<String>();

        for (Answer answer : answerList)
            if (answer.getFilePath() != null && answer.getOwner().equals(studentCrsId))
                questionPathList.add(answer.getFilePath());

        PDFManip.PdfMerge(questionPathList, "tempo.pdf");

        PDFManip.PdfMetadataInject("page.user.1", "ap760", "tempo.pdf");
        PDFManip.PdfMetadataInject("page.user.2", "ap760", "tempo.pdf");
        PDFManip.PdfMetadataInject("page.user.3", "ap760", "tempo.pdf");
        PDFManip.PdfMetadataInject("page.user.4", "ap760", "tempo.pdf");
        PDFManip.PdfMetadataInject("page.user.5", "ap760", "tempo.pdf");
        PDFManip.PdfMetadataInject("page.question.1", "ap760", "tempo.pdf");
        PDFManip.PdfMetadataInject("page.question.2", "ap760", "tempo.pdf");
        PDFManip.PdfMetadataInject("page.question.3", "ap760", "tempo.pdf");
        PDFManip.PdfMetadataInject("page.question.4", "ap760", "tempo.pdf");
        PDFManip.PdfMetadataInject("page.question.5", "ap760", "tempo.pdf");

        return Response.ok(new File("tempo.pdf")).build();
    }  /*

    @GET
    @Path("/question/{questionDetail}")
    public Object viewQuestion(@PathParam("binId") long binId, @PathParam("questionDetail") String questionDetail) {

        // Set Hibernate and get user
        Session session = HibernateUtil.getSession();

        String user = UserHelper.getCurrentUser();

        // Get Bin and check
        Bin bin = BinController.getBin(binId);

        if (bin == null)
            return Response.status(401).build();

        List<Answer> allAnswers = new LinkedList<Answer>(bin.getAnswers());

        Map<String, List<AnsweredQuestion>> studentQuestions = new HashMap<String, List<AnsweredQuestion>>();
        Set<String> studentSet = new TreeSet<String>();

        for (Answer answer : allAnswers) {

            if (bin.canSeeAnswer(user, answer))
            {
                if (!studentSet.contains(answer.getOwner())) {
                    studentQuestions.put(answer.getOwner(), new LinkedList<AnsweredQuestion>());

                    studentSet.add(answer.getOwner());
                }

                boolean marked = answer.getMarkedAnswers().size() > 0;

                AnsweredQuestion q = new AnsweredQuestion(answer.getQuestion(), answer.getFilePath(), true, marked);

                studentQuestions.get(answer.getOwner()).add(q);
            }
        }

        List<StudentSubmission> studentSubmissionList = new LinkedList<StudentSubmission>();

        for (String s : studentSet) {
            StudentSubmission studentSubmission = new StudentSubmission();

            studentSubmission.setName(s);
            studentSubmission.setAnsweredQuestions(studentQuestions.get(s));
            studentSubmission.setMarked(studentSubmission.getAnsweredQuestions().size() == bin.getQuestionCount());

            studentSubmissionList.add(studentSubmission);
        }

        return ImmutableMap.of("studentSubmissionList", studentSubmissionList);
    }

    @GET
    @Path("/all")
    public Object viewAll(@PathParam("binId") long binId) {

        // Set Hibernate and get user
        Session session = HibernateUtil.getSession();

        String user = UserHelper.getCurrentUser();

        // Get Bin and check
        Bin bin = BinController.getBin(binId);

        if (bin == null)
            return Response.status(401).build();

        List<Answer> allAnswers = new LinkedList<Answer>(bin.getAnswers());

        Map<String, List<AnsweredQuestion>> studentQuestions = new HashMap<String, List<AnsweredQuestion>>();
        Set<String> studentSet = new TreeSet<String>();

        for (Answer answer : allAnswers) {

            if (bin.canSeeAnswer(user, answer))
            {
                if (!studentSet.contains(answer.getOwner())) {
                    studentQuestions.put(answer.getOwner(), new LinkedList<AnsweredQuestion>());

                    studentSet.add(answer.getOwner());
                }

                boolean marked = answer.getMarkedAnswers().size() > 0;

                AnsweredQuestion q = new AnsweredQuestion(answer.getQuestion(), answer.getFilePath(), true, marked);

                studentQuestions.get(answer.getOwner()).add(q);
            }
        }

        List<StudentSubmission> studentSubmissionList = new LinkedList<StudentSubmission>();

        for (String s : studentSet) {
            StudentSubmission studentSubmission = new StudentSubmission();

            studentSubmission.setName(s);
            studentSubmission.setAnsweredQuestions(studentQuestions.get(s));
            studentSubmission.setMarked(studentSubmission.getAnsweredQuestions().size() == bin.getQuestionCount());

            studentSubmissionList.add(studentSubmission);
        }

        return ImmutableMap.of("studentSubmissionList", studentSubmissionList);
    }                   */
}
