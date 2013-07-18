package uk.ac.cam.sup.tools;

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
    public static void distributeSubmission(Submission submission) {

        try {
            // Set Hibernate and get file to be split
            Session session = HibernateUtil.getSession();

            List <Distribution> distributions = submission.getSubmissionDistribution();

            for (Distribution distribution : distributions)
            {
                // Create directory
                String directory = "temp/" + distribution.getStudent() + "/" + submission.getFolder() + "/";
                File fileDirectory = new File(directory);
                fileDirectory.mkdirs();

                // New Answer and get id
                Answer answer = new Answer();

                session.save(answer);

                session.getTransaction().commit();

                // Restart session
                session = HibernateUtil.getSession();
                session.beginTransaction();

                // Save Answer
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
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /*

     */
    public static void mergePdf(PDFManip pdfManip, List<String> filePaths) throws IOException {
        fileMove(filePaths.get(0), pdfManip.getFilePath());
        filePaths.remove(0);

        for (String filePath : filePaths)
            pdfManip.add(filePath);
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
