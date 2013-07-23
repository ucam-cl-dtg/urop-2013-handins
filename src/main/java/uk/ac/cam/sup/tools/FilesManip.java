package uk.ac.cam.sup.tools;

import com.itextpdf.text.DocumentException;
import org.apache.commons.io.FileUtils;
import org.hibernate.Session;
import uk.ac.cam.sup.HibernateUtil;
import uk.ac.cam.sup.exceptions.MetadataNotFoundException;
import uk.ac.cam.sup.models.*;
import uk.ac.cam.sup.structures.Distribution;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.LinkedList;
import java.util.List;

public class FilesManip {

    /*

     */
    public static void distributeSubmission(Submission submission) throws MetadataNotFoundException, IOException, DocumentException {

        // Set Hibernate and get file to be split
        Session session = HibernateUtil.getSession();

        List <Distribution> distributions = submission.getSubmissionDistribution();

        for (Distribution distribution : distributions)
        {
            // Create directory
            String directory = "temp/" + distribution.getStudent() + "/" + submission.getFolder() + "/";
            File fileDirectory = new File(directory);
            fileDirectory.mkdirs();

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
    }

    /*

     */
    public static boolean mergePdf(PDFManip pdfManip, List<String> filePaths) throws IOException, DocumentException {
        if (filePaths.size() == 0)
            return false;

        fileCopy(filePaths.get(0), pdfManip.getFilePath());
        filePaths.remove(0);

        for (String filePath : filePaths)
            pdfManip.add(filePath);

        return true;
    }

    /*
    Todo: maybe delete or change the function
     */
    public static void markPdf(PDFManip pdfManip, String owner, String question) throws IOException, DocumentException {
        for (int i = 1; i <= pdfManip.getPageCount(); i++) {
            pdfManip.injectMetadata("page.owner." + i, owner);
            pdfManip.injectMetadata("page.question." + i, Integer.toString((1 + i) / 2));
        }
    }

    /*
    Takes the array of bytes representing the data to be written and the destination path
    and writes the data to the new file created.
     */
    public static void fileSave(byte[] data, String destination) throws IOException {
        File destinationFile = new File(destination);
        OutputStream outputStream = new FileOutputStream(destinationFile);

        outputStream.write(data);
        outputStream.close();
    }

    /*
    Takes the path of a file and deletes the file.
     */
    public static void fileDelete(String filePath) {
        File f = new File(filePath);

        f.delete();
    }

    /*

     */
    public static void fileCopy(String source, String destination) throws IOException {
        File sourceFile = new File(source);
        File destinationFile = new File(destination);

        FileUtils.copyFile(sourceFile, destinationFile);
    }

    /*
    Takes the path of the source and the destination where the file should be moved.
    Copies the file to the new location and deletes the original one.
     */
    public static void fileMove(String source, String destination) throws IOException {
        File sourceFile = new File(source);
        File destinationFile = new File(destination);

        FileUtils.copyFile(sourceFile, destinationFile);
        sourceFile.delete();
    }
}
