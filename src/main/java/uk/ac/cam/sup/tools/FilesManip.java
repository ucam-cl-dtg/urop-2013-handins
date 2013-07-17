package uk.ac.cam.sup.tools;

import com.itextpdf.text.Document;
import com.itextpdf.text.pdf.PdfCopy;
import com.itextpdf.text.pdf.PdfReader;
import org.apache.commons.io.FileUtils;
import org.hibernate.Session;
import uk.ac.cam.sup.HibernateUtil;
import uk.ac.cam.sup.exceptions.MetadataNotFoundException;
import uk.ac.cam.sup.models.Answer;
import uk.ac.cam.sup.models.MarkedSubmission;
import uk.ac.cam.sup.models.Submission;
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
    public static void distributeSubmission(Submission submission) {

        try {
            // Set Hibernate and get file to be split
            Session session = HibernateUtil.getSession();

            List <Distribution> distributions = PDFManip.getSubmissionDistribution(submission);

            for (Distribution distribution : distributions)
            {
                String location = "temp/" + distribution.getStudent() + "/answers/";

                // New Answer and get id
                Answer answer = new Answer();

                session.save(answer);

                session.getTransaction().commit();

                // Restart session
                session = HibernateUtil.getSession();
                session.beginTransaction();

                // Save Answer
                String filePath = location + answer.getId() + ".pdf";
                PDFManip.PdfTakePages(submission.getFilePath(), distribution.getStartPage(), distribution.getEndPage(), filePath);
                PDFManip.PdfAddHeader(distribution.getStudent() + " " + distribution.getQuestion(), filePath);

                // ToDo: Bin
                answer.setFilePath(filePath);
                answer.setQuestion(distribution.getQuestion());
                answer.setFinalState(false);
                answer.setOwner(distribution.getStudent());
                answer.setSubmission(submission);

                session.update(answer);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /*

     */
    public static void distributeMarkedSubmission(MarkedSubmission markedSubmission) {

        try {
            // Set Hibernate and get file to be split
            Session session = HibernateUtil.getSession();

            List <Distribution> distributions = PDFManip.getMarkedSubmissionDistribution(markedSubmission);

            for (Distribution distribution : distributions)
            {
                String location = "temp/" + distribution.getStudent() + "/annotated/";

                // New Answer and get id
                Answer answer = new Answer();

                session.save(answer);

                session.getTransaction().commit();

                // Restart session
                session = HibernateUtil.getSession();
                session.beginTransaction();

                // Save Answer
                String filePath = location + answer.getId() + ".pdf";
                PDFManip.PdfTakePages(markedSubmission.getFilePath(), distribution.getStartPage(), distribution.getEndPage(), filePath);
            }
        } catch (Exception e) {
            e.printStackTrace();
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

     */
    public static void fileDelete(String filePath) {
        File f = new File(filePath);

        f.delete();
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
