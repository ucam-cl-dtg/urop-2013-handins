package uk.ac.cam.sup.tools;

import com.itextpdf.text.Document;
import com.itextpdf.text.Image;
import com.itextpdf.text.pdf.PdfCopy;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfWriter;
import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.hibernate.Session;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import uk.ac.cam.sup.HibernateUtil;
import uk.ac.cam.sup.models.*;
import uk.ac.cam.sup.structures.Distribution;
import uk.ac.cam.sup.structures.Marking;

import javax.ws.rs.core.Response;
import java.io.*;
import java.nio.file.*;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class FilesManip {

    private static void extractFiles(String zipPath, String destinationFolder) throws Exception {
        ZipFile zipFile = new ZipFile(zipPath);

        zipFile.extractAll(destinationFolder);
    }

    private static Set<File> getFilesFromFolder(File folder) {
        Set<File> files = new TreeSet<File>();

        //noinspection ConstantConditions
        for (File fileEntry : folder.listFiles()) {
            if (fileEntry.isDirectory())
                files.addAll(getFilesFromFolder(fileEntry));
            else files.add(fileEntry);
        }

        return files;
    }

    public static void manage(String directory, String baseName, String destination) {
        try {
            String type = Files.probeContentType(FileSystems.getDefault().getPath(directory, baseName));

            // Create directory
            File fileDirectory = new File(directory + "elem/");
            //noinspection ResultOfMethodCallIgnored
            fileDirectory.mkdirs();

            // Move everything in /elem
            if (type.equals("application/zip"))
                extractFiles(directory + baseName, directory + "elem/");
            else
                fileMove(directory + baseName, directory + "elem/" + baseName);

            Set<File> files = getFilesFromFolder(new File(directory + "elem/"));

            for (File file : files) {
                if (Files.probeContentType(FileSystems.getDefault().getPath(file.getAbsolutePath())).equals("application/pdf"))
                    fileMove(file.getAbsolutePath(), directory + "pdf/" + file.getName());
                else {
                    Document document = new Document();

                    PdfWriter.getInstance(document, new FileOutputStream(directory + "pdf/" + file.getName()));
                    document.open();

                    Image image1 = Image.getInstance(file.getAbsolutePath());
                    document.add(image1);

                    document.close();
                }
            }

            files = getFilesFromFolder(new File(directory + "pdf/"));
            Set<String> filePaths = new TreeSet<String>();

            for (File file : files)
                filePaths.add(file.getAbsolutePath());

            mergePdf(new LinkedList<String>(filePaths), destination);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /*
    Done
     */
    public static Object resultingFile(List<Marking> questionList) {

        // Create directory
        String directory = "temp/";
        File fileDirectory = new File(directory);
        //noinspection ResultOfMethodCallIgnored
        fileDirectory.mkdirs();

        String randomTemp = "temp/temp" + RandomStringUtils.randomAlphabetic(4) + ".pdf";

        List<String> questionPathList = new LinkedList<String>();
        for (Marking marking : questionList)
            questionPathList.add(marking.getFilePath());

        PDFManip pdfManip;
        try {
            FilesManip.mergePdf(questionPathList, randomTemp);

            pdfManip = new PDFManip(randomTemp);
        } catch (Exception e) {
            return Response.status(404).build();
        }

        for (Marking marking : questionList)
            FilesManip.markPdf(pdfManip, marking.getOwner(), marking.getQuestion(), marking.getFirst(), marking.getLast());

        try {
            return Response.ok(new TemporaryFileInputStream(new File(randomTemp))).build();
        } catch (Exception e) {
            return Response.status(404).build();
        }
    }

    /*
    Done
     */
    private static void rememberAnswer(Distribution distribution, String directory, Submission submission) {

        // Set Hibernate
        Session session = HibernateUtil.getSession();

        // New Answer to get id
        Answer answer = new Answer();
        session.save(answer);

        // Update Answer
        String filePath = directory + answer.getId() + ".pdf";
        try {
            new PDFManip(submission.getFilePath()).takePages(distribution.getStartPage(), distribution.getEndPage(), filePath);
        } catch (Exception e) {
            e.printStackTrace();
        }

        for (Object object : session.createCriteria(Answer.class)
                                     .add(Restrictions.eq("bin", submission.getBin()))
                                     .add(Restrictions.eq("question", distribution.getQuestion()))
                                     .add(Restrictions.eq("owner", distribution.getStudent()))
                                     .list()) {

            Answer answer1 = (Answer) object;

            answer1.setLast(false);
        }


        answer.setBin(submission.getBin());
        answer.setFilePath(filePath);
        answer.setQuestion(distribution.getQuestion());
        answer.setLast(true);
        answer.setOwner(distribution.getStudent());
        answer.setUnmarkedSubmission((UnmarkedSubmission) submission);

        session.update(answer);
    }

    /*
    Done
     */
    private static void rememberMarkedAnswer(String user, Distribution distribution, String directory, Submission submission) {

        // Set Hibernate
        Session session = HibernateUtil.getSession();

        // New markedAnswer to get id
        MarkedAnswer markedAnswer = new MarkedAnswer();
        session.save(markedAnswer);

        // Update Answer
        String filePath = directory + markedAnswer.getId() + ".pdf";
        try {
            new PDFManip(submission.getFilePath()).takePages(distribution.getStartPage(), distribution.getEndPage(), filePath);
        } catch (Exception e) {
            e.printStackTrace();
        }

        markedAnswer.setFilePath(filePath);
        markedAnswer.setOwner(distribution.getStudent());
        markedAnswer.setAnnotator(user);
        markedAnswer.setMarkedSubmission((MarkedSubmission) submission);
        markedAnswer.setAnswer((Answer) session.createCriteria(Answer.class)
                                               .add(Restrictions.eq("owner", distribution.getStudent()))
                                               .add(Restrictions.eq("question", distribution.getQuestion()))
                                               .addOrder(Order.desc("dateCreated"))
                                               .list().get(0));
        markedAnswer.getAnswer().setAnnotated(true);

        session.update(markedAnswer);
    }

    /*
    Done
     */
    public static void distributeSubmission(String user, Submission submission) {

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
                rememberMarkedAnswer(user, distribution, directory, submission);
        }
    }

    /*
    Done
     */
    public static void mergePdf(List<String> filePaths, String destination) throws Exception {

        if (filePaths.size() == 0)
            return;

        Document document = new Document();

        PdfCopy copy = new PdfCopy(document, new FileOutputStream(destination));

        document.open();

        for (String filePath : filePaths) {

            PdfReader reader;
            try {
                reader = new PdfReader(filePath);
            } catch (IOException e) {
                continue;
            }
            int n = reader.getNumberOfPages();

            for (int pn = 0; pn < n; )
                try {
                    copy.addPage(copy.getImportedPage(reader, ++pn));
                } catch (Exception e) {
                    e.printStackTrace();
                }
        }

        document.close();
    }

    /*
    Todo: maybe delete or change the function
     */
    public static void markPdf(PDFManip pdfManip, String owner, ProposedQuestion question, int firstPage, int lastPage) {
        for (int i = firstPage; i <= lastPage; i++) {
            try {
                pdfManip.injectMetadata("pageOwner" + i, owner);
                pdfManip.injectMetadata("pageQuestion" + i, Long.toString(question.getId()));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /*
    Takes the array of bytes representing the data to be written and the destination path
    and writes the data to the new file created.
    Done
     */
    public static void fileSave(byte[] data, String destination) throws Exception {
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
     /
    public static void fileCopy(String source, String destination) throws Exception {
        File sourceFile = new File(source);
        File destinationFile = new File(destination);

        FileUtils.copyFile(sourceFile, destinationFile);
    }
    */
    /*
    Takes the path of the source and the destination where the file should be moved.
    Copies the file to the new location and deletes the original one.
    Done
     */
    public static void fileMove(String source, String destination) throws Exception {
        File sourceFile = new File(source);
        File destinationFile = new File(destination);

        FileUtils.copyFile(sourceFile, destinationFile);
        //noinspection ResultOfMethodCallIgnored
        sourceFile.delete();
    }
}
