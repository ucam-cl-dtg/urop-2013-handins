package uk.ac.cam.sup.controllers;

import com.google.common.collect.ImmutableMap;
import com.itextpdf.text.DocumentException;
import org.apache.commons.lang3.RandomStringUtils;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;
import org.jboss.resteasy.annotations.providers.multipart.MultipartForm;
import uk.ac.cam.sup.HibernateUtil;
import uk.ac.cam.sup.exceptions.MetadataNotFoundException;
import uk.ac.cam.sup.forms.FileUploadForm;
import uk.ac.cam.sup.helpers.UserHelper;
import uk.ac.cam.sup.models.*;
import uk.ac.cam.sup.structures.Pair;
import uk.ac.cam.sup.structures.Quad;
import uk.ac.cam.sup.structures.Triple;
import uk.ac.cam.sup.tools.FilesManip;
import uk.ac.cam.sup.tools.PDFManip;
import uk.ac.cam.sup.tools.TemporaryFileInputStream;

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

        session.getTransaction().commit();

        // Restart session
        session = HibernateUtil.getSession();
        session.beginTransaction();

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

    /*
    Done
     */
    public Object resultingFile(List<String> questionPathList) throws IOException, DocumentException {

        String randomTemp = "temp/temp" + RandomStringUtils.randomAlphabetic(4) + ".pdf";
        PDFManip pdfManip = new PDFManip(randomTemp);

        if (FilesManip.mergePdf(pdfManip, questionPathList))
            FilesManip.markPdf(pdfManip, "ap760", Integer.toString(3));
        else return Response.status(401).build();

        return Response.ok(new TemporaryFileInputStream(new File(randomTemp))).build();
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
        List<Pair<String, Boolean>> students = new LinkedList<Pair<String, Boolean>>();

        for (BinPermission permission : allAccess) {
            String student = permission.getUser();
            boolean marked = false;

            int markedAnswers = session.createCriteria(MarkedAnswer.class)
                                       .add(Restrictions.eq("owner", student))
                                       .add(Restrictions.eq("bin", bin))
                                       .list().size();

            if (markedAnswers == bin.getQuestionCount())
                marked = true;

            List<Answer> answers = session.createCriteria(Answer.class)
                                          .add(Restrictions.eq("owner", student))
                                          .add(Restrictions.eq("bin", bin))
                                          .list();

            boolean available = false;
            for (Answer answer : answers)
                if (bin.canSeeAnswer(user, answer))
                    available = true;

            if (available)
                students.add(new Pair<String, Boolean>(student, marked));
        }

        return ImmutableMap.of("List of Student Pairs: <crsId/link, isMarked>", students);
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
        List<Quad<String, Long, Boolean, Boolean>> studentQuestions = new LinkedList<Quad<String, Long, Boolean, Boolean>>();

        for (ProposedQuestion question : questions) {
            List<Answer> answers = session.createCriteria(Answer.class)
                                          .add(Restrictions.eq("owner", studentCrsId))
                                          .add(Restrictions.eq("bin", bin))
                                          .add(Restrictions.eq("question", question)).list();

            boolean exists = answers.size() > 0;

            if (exists)
                if (bin.canSeeAnswer(user, answers.get(0))) {
                    boolean isMarked = answers.get(0).getMarkedAnswers().size() > 0;

                    studentQuestions.add(new Quad<String, Long, Boolean, Boolean>(question.getName(), question.getId(), exists, isMarked));
                }
        }

        return ImmutableMap.of("List of Question Quads: <name, id/link, isSubmitted, isMarked>", studentQuestions);
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

        List<String> pathList = new LinkedList<String>();
        for (Answer answer : answers)
            if (answer.getOwner().equals(studentCrsId) && bin.canSeeAnswer(user, answer))
                pathList.add(answer.getFilePath());

        return resultingFile(pathList);
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
        List<Triple<String, Long, Boolean>> questionList = new LinkedList<Triple<String, Long, Boolean>>();

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
                questionList.add(new Triple<String, Long, Boolean>(question.getName(), question.getId(), isMarked));
        }

        return ImmutableMap.of("List of Question Triple: <name, id/link, isMarked>", questionList);
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
        List<Pair<String, Boolean>> studentList = new LinkedList<Pair<String, Boolean>>();

        for (Answer answer : answers)
            if (bin.canSeeAnswer(user, answer))
                studentList.add(new Pair<String, Boolean>(answer.getOwner(), answer.getMarkedAnswers().size() > 0));

        return ImmutableMap.of("List of Student of Pair: <crsId/link, isMarked>", studentList);
    }
     /*
    @GET
    @Path("/bin/{binId}/question/{questionId}/download")
    @Produces("application/pdf")
    public Object getQuestion(@PathParam("binId") long binId, @PathParam("questionId") String questionId) {

        // Get Bin and check
        Bin bin = BinController.getBin(binId);

        if (bin == null)
            return Response.status(401).build();

        return null;
    }

    @GET
    @Path("/bin/{binId}/student/{studentCrsId}/question/{questionId}")
    @Produces("application/pdf")
    public Object viewStudentQuestion(@PathParam("binId") long binId,
                                      @PathParam("studentCrsId") String studentCrsId,
                                      @PathParam("questionId}") long questionId) throws IOException, DocumentException {

        // Get Bin and check
        Bin bin = BinController.getBin(binId);

        if (bin == null)
            return Response.status(401).build();



        return resultingFile(questionPathList);
    }
    /*
    Done
     */  /*
    @GET
    @Path("/bin/{binId}/question/{questionId}/student/{studentCrsId}")
    @Produces("application/pdf")
    public Object viewQuestionStudent(@PathParam("binId") long binId,
                                      @PathParam("questionId") long questionId,
                                      @PathParam("studentCrsId") String studentCrsId) throws IOException, DocumentException {

        return viewStudentQuestion(binId, studentCrsId, questionId);
    }
    */

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
        List<String> questionPathList = new LinkedList<String>();

        for (Answer answer : answers)
            if (bin.canSeeAnswer(user, answer))
                questionPathList.add(answer.getFilePath());

        return resultingFile(questionPathList);
    }
}
