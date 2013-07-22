package uk.ac.cam.sup.controllers;

import com.google.common.collect.ImmutableMap;
import com.itextpdf.text.DocumentException;
import org.apache.commons.lang3.RandomStringUtils;
import org.hibernate.Session;
import org.jboss.resteasy.annotations.providers.multipart.MultipartForm;
import uk.ac.cam.sup.HibernateUtil;
import uk.ac.cam.sup.exceptions.MetadataNotFoundException;
import uk.ac.cam.sup.forms.FileUploadForm;
import uk.ac.cam.sup.helpers.UserHelper;
import uk.ac.cam.sup.models.*;
import uk.ac.cam.sup.structures.StudentSubmissions;
import uk.ac.cam.sup.structures.StudentSubmissions.AnsweredQuestion;
import uk.ac.cam.sup.tools.FilesManip;
import uk.ac.cam.sup.tools.PDFManip;
import uk.ac.cam.sup.tools.TemporaryFileInputStream;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.*;

@Path("/marking")
public class MarkingController {

    public Object resultingFile(List<String> questionPathList) throws IOException, DocumentException {

        String randomTemp = "temp" + RandomStringUtils.randomAlphabetic(4) + ".pdf";
        PDFManip pdfManip = new PDFManip(randomTemp);

        if (FilesManip.mergePdf(pdfManip, questionPathList))
            FilesManip.markPdf(pdfManip, "ap760", Integer.toString(3));
        else return Response.status(401).build();

        return Response.ok(new TemporaryFileInputStream(new File(randomTemp))).build();
    }

    public Map <String, List <StudentSubmissions>> getStudentList(Bin bin) {

        // Get user
        String user = UserHelper.getCurrentUser();

        List<Answer> allAnswers = new LinkedList<Answer>(bin.getAnswers());

        Set<String> studentSet = new TreeSet<String>();
        Map<String, StudentSubmissions> studentMap = new HashMap<String, StudentSubmissions>();

        for (Answer answer : allAnswers) {

            if (bin.canSeeAnswer(user, answer)) {

                if (!studentSet.contains(answer.getOwner())) {
                    studentSet.add(answer.getOwner());
                    studentMap.put(answer.getOwner(), new StudentSubmissions(answer.getOwner(), false));
                }

                studentMap.get(answer.getOwner()).addAnsweredQuestions(answer.getQuestion(), answer.getFilePath(),
                        "/bin/" + bin.getId() + "/student/" + answer.getOwner(), true, answer.getMarkedAnswers().size() > 0);
            }
        }

        List<StudentSubmissions> studentSubmissionsList = new LinkedList<StudentSubmissions>();

        for (String student : studentSet) {

            StudentSubmissions studentSubmissions = studentMap.get(student);

            studentSubmissions.setMarked(studentSubmissions.getAnsweredQuestions().size() == bin.getQuestionCount());

            studentSubmissionsList.add(studentSubmissions);
        }

        return ImmutableMap.of("students", studentSubmissionsList);
    }

    @POST
    @Path("/bin/{binId}")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces("application/json")
    public Object createMarkedSubmission(@MultipartForm FileUploadForm uploadForm, @PathParam("binId") long binId) throws IOException, MetadataNotFoundException, DocumentException {

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
        fileDirectory.mkdirs();

        String fileName = "submission_" + markedSubmission.getId() + ".pdf";

        FilesManip.fileSave(uploadForm.file, directory + fileName);

        markedSubmission.setFilePath(directory + fileName);

        session.update(markedSubmission);

        List<String> listOfUploads = null;
        FilesManip.distributeSubmission(markedSubmission);

        return ImmutableMap.of("id", markedSubmission.getId(), "User/Question List", listOfUploads);
    }

    @GET
    @Path("/bin/{binId}/student")
    @Produces("application/json")
    public Object viewAllStudentSubmissions(@PathParam("binId") long binId) {

        // Get Bin and check
        Bin bin = BinController.getBin(binId);

        if (bin == null)
            return Response.status(401).build();

        return getStudentList(bin);
    }

    @GET
    @Path("/bin/{binId}/student/{studentCrsId}")
    @Produces("application/pdf")
    public Object viewStudent(@PathParam("binId") long binId, @PathParam("studentCrsId") String studentCrsId) throws IOException, DocumentException {

        // Get Bin and check
        Bin bin = BinController.getBin(binId);

        if (bin == null)
            return Response.status(404).build();

        List <StudentSubmissions> allAnswers = getStudentList(bin).get("students");
        List <String> questionPathList = new LinkedList<String>();

        for (StudentSubmissions studentSubmissions : allAnswers)
            if (studentSubmissions.getName().equals(studentCrsId))
                for (AnsweredQuestion answeredQuestion : studentSubmissions.getAnsweredQuestions())
                    questionPathList.add(answeredQuestion.getFilePath());

        String randomTemp = "temp" + RandomStringUtils.randomAlphabetic(4) + ".pdf";
        PDFManip pdfManip = new PDFManip(randomTemp);

        if (FilesManip.mergePdf(pdfManip, questionPathList))
            FilesManip.markPdf(pdfManip, "ap760", Integer.toString(3));
        else return Response.status(401).build();

        return Response.ok(new TemporaryFileInputStream(new File(randomTemp))).build();
    }

    @GET
    @Path("/bin/{binId}/question")
    @Produces("application/json")
    public Object viewAllQuestionSubmissions(@PathParam("binId") long binId) {

        // Get Bin and check
        Bin bin = BinController.getBin(binId);

        if (bin == null)
            return Response.status(401).build();

        return null;
    }

    @GET
    @Path("/bin/{binId}/question/{questionId}")
    @Produces("application/pdf")
    public Object viewQuestion(@PathParam("binId") long binId, @PathParam("questionId") String questionId) {

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
                                      @PathParam("studentCrsId") long studentCrsId,
                                      @PathParam("questionId}") long questionId) throws IOException, DocumentException {

        // Get Bin and check
        Bin bin = BinController.getBin(binId);

        if (bin == null)
            return Response.status(401).build();

        List <StudentSubmissions> allAnswers = getStudentList(bin).get("students");
        List <String> questionPathList = new LinkedList<String>();

        for (StudentSubmissions studentSubmissions : allAnswers)
            if (studentSubmissions.getName().equals(studentCrsId))
                for (StudentSubmissions.AnsweredQuestion answeredQuestion : studentSubmissions.getAnsweredQuestions())
                    if (answeredQuestion.getQuestion().getId() == questionId)
                        questionPathList.add(answeredQuestion.getFilePath());

        return resultingFile(questionPathList);
    }

    @GET
    @Path("/bin/{binId}/all")
    @Produces("application/pdf")
    public Object viewAll(@PathParam("binId") long binId) throws IOException, DocumentException {

        // Get Bin and check
        Bin bin = BinController.getBin(binId);

        if (bin == null)
            return Response.status(401).build();

        List <StudentSubmissions> allAnswers = getStudentList(bin).get("students");
        List <String> questionPathList = new LinkedList<String>();

        for (StudentSubmissions studentSubmissions : allAnswers)
            for (StudentSubmissions.AnsweredQuestion answeredQuestion : studentSubmissions.getAnsweredQuestions())
                questionPathList.add(answeredQuestion.getFilePath());

        return resultingFile(questionPathList);
    }
}
