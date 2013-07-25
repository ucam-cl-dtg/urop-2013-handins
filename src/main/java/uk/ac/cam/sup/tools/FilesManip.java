package uk.ac.cam.sup.tools;

import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.pdf.PdfCopy;
import com.itextpdf.text.pdf.PdfReader;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;
import uk.ac.cam.sup.HibernateUtil;
import uk.ac.cam.sup.exceptions.MetadataNotFoundException;
import uk.ac.cam.sup.helpers.UserHelper;
import uk.ac.cam.sup.models.*;
import uk.ac.cam.sup.structures.Distribution;
import uk.ac.cam.sup.structures.Marking;

import javax.ws.rs.core.Response;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.LinkedList;
import java.util.List;

public class FilesManip {

    /*
    Done
     */
    public static Object resultingFile(List<Marking> questionList) throws IOException, DocumentException {

        // Create directory
        String directory = "temp/";
        File fileDirectory = new File(directory);
        //noinspection ResultOfMethodCallIgnored
        fileDirectory.mkdirs();

        String randomTemp = "temp/temp" + RandomStringUtils.randomAlphabetic(4) + ".pdf";

        List<String> questionPathList = new LinkedList<String>();
        for (Marking marking : questionList)
            questionPathList.add(marking.getFilePath());

        FilesManip.mergePdf(questionPathList, randomTemp);
        PDFManip pdfManip = new PDFManip(randomTemp);

        for (Marking marking : questionList)
            FilesManip.markPdf(pdfManip, marking.getOwner(), marking.getQuestion(), marking.getFirst(), marking.getLast());

        return Response.ok(new TemporaryFileInputStream(new File(randomTemp))).build();
    }

    /*
    Done
     */
    private static void rememberAnswer(Distribution distribution, String directory, Submission submission) throws IOException, DocumentException {

        // Set Hibernate
        Session session = HibernateUtil.getSession();

        // New Answer to get id
        Answer answer = new Answer();
        session.save(answer);

        // Update Answer
        String filePath = directory + answer.getId() + ".pdf";
        PDFManip pdfManip = new PDFManip(filePath);
        new PDFManip(submission.getFilePath()).takePages(distribution.getStartPage(), distribution.getEndPage(), filePath);
        pdfManip.addHeader(distribution.getStudent() + " " + distribution.getQuestion());

        answer.setBin(submission.getBin());
        answer.setFilePath(filePath);
        answer.setQuestion(distribution.getQuestion());
        answer.setFinalState(false);
        answer.setOwner(distribution.getStudent());
        answer.setUnmarkedSubmission((UnmarkedSubmission) submission);

        session.update(answer);
    }

    /*
    Done
     */
    private static void rememberMarkedAnswer(Distribution distribution, String directory, Submission submission) throws IOException, DocumentException {

        // Set Hibernate
        Session session = HibernateUtil.getSession();

        // New markedAnswer to get id
        MarkedAnswer markedAnswer = new MarkedAnswer();
        session.save(markedAnswer);

        // Update Answer
        String filePath = directory + markedAnswer.getId() + ".pdf";
        new PDFManip(submission.getFilePath()).takePages(distribution.getStartPage(), distribution.getEndPage(), filePath);

        markedAnswer.setFilePath(filePath);
        markedAnswer.setOwner(distribution.getStudent());
        markedAnswer.setAnnotator(UserHelper.getCurrentUser());
        markedAnswer.setMarkedSubmission((MarkedSubmission) submission);
        markedAnswer.setAnswer((Answer) session.createCriteria(Answer.class)
                                      .add(Restrictions.eq("owner", distribution.getStudent()))
                                      .add(Restrictions.eq("question", distribution.getQuestion()))
                                      .list().get(0));
        markedAnswer.getAnswer().setAnnotated(true);

        session.update(markedAnswer);
    }

    /*
    Done
     */
    public static void distributeSubmission(Submission submission) throws IOException, DocumentException {

        // Split the file

        @SuppressWarnings("unchecked")
        List <Distribution> distributions = submission.getSubmissionDistribution();

        for (Distribution distribution : distributions)
        {
            // Create directory
            String directory = "temp/" + distribution.getStudent() + "/" + submission.getFolder() + "/";
            File fileDirectory = new File(directory);
            //noinspection ResultOfMethodCallIgnored
            fileDirectory.mkdirs();

            if (submission instanceof UnmarkedSubmission)
                rememberAnswer(distribution, directory, submission);
            if (submission instanceof MarkedSubmission)
                rememberMarkedAnswer(distribution, directory, submission);
        }
    }

    /*
    Done
     */
    public static void mergePdf(List<String> filePaths, String destination) throws IOException, DocumentException {

        if (filePaths.size() == 0)
            return;

        Document document = new Document();

        PdfCopy copy = new PdfCopy(document, new FileOutputStream(destination));

        document.open();

        for (String filePath : filePaths) {

            PdfReader reader = new PdfReader(filePath);
            int n = reader.getNumberOfPages();

            for (int pn = 0; pn < n; )
                copy.addPage(copy.getImportedPage(reader, ++pn));
        }

        document.close();
    }

    /*
    Todo: maybe delete or change the function
     */
    public static void markPdf(PDFManip pdfManip, String owner, ProposedQuestion question, int firstPage, int lastPage) throws IOException, DocumentException {
        for (int i = firstPage; i <= lastPage; i++) {
            pdfManip.injectMetadata("page.owner." + i, owner);
            pdfManip.injectMetadata("page.question." + i, Long.toString(question.getId()));
        }
    }

    /*
    Takes the array of bytes representing the data to be written and the destination path
    and writes the data to the new file created.
    Done
     */
    public static void fileSave(byte[] data, String destination) throws IOException {
        File destinationFile = new File(destination);
        OutputStream outputStream = new FileOutputStream(destinationFile);

        outputStream.write(data);
        outputStream.close();
    }

    /*
    Takes the path of a file and deletes the file.
    Done
     */
    public static void fileDelete(String filePath) {
        File f = new File(filePath);

        //noinspection ResultOfMethodCallIgnored
        f.delete();
    }

    /*
    Done
     */
    public static void fileCopy(String source, String destination) throws IOException {
        File sourceFile = new File(source);
        File destinationFile = new File(destination);

        FileUtils.copyFile(sourceFile, destinationFile);
    }

    /*
    Takes the path of the source and the destination where the file should be moved.
    Copies the file to the new location and deletes the original one.
    Done
     */
    public static void fileMove(String source, String destination) throws IOException {
        File sourceFile = new File(source);
        File destinationFile = new File(destination);

        FileUtils.copyFile(sourceFile, destinationFile);
        //noinspection ResultOfMethodCallIgnored
        sourceFile.delete();
    }
}
