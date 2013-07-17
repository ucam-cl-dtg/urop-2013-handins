package uk.ac.cam.sup.tools;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import uk.ac.cam.sup.exceptions.MetadataNotFoundException;
import uk.ac.cam.sup.models.MarkedSubmission;
import uk.ac.cam.sup.models.Submission;
import uk.ac.cam.sup.structures.Distribution;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class PDFManip {
    /*
    FixMe: This function should be rewritten because it behaves badly in general
     */
    public static boolean PdfAddHeader(String content, String filePath) {

        try {
            String temp = "temp.pdf";

            FilesManip.fileMove(filePath, temp);

            PdfReader reader = new PdfReader(temp);
            PdfStamper stamper = new PdfStamper(reader, new FileOutputStream(filePath));

            int n = reader.getNumberOfPages();
            for (int i = 1; i <= n; i++)
                getHeaderTable(i, n, content).writeSelectedRows(0, -1, 30, 795, stamper.getOverContent(i));

            stamper.close();

            FilesManip.fileDelete(temp);

            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /*
    Don't mind this... trust me
     */
    private static PdfPTable getHeaderTable(int x, int y, String content) {
        PdfPTable table = new PdfPTable(2);
        table.setTotalWidth(530);
        table.setLockedWidth(true);
        table.getDefaultCell().setFixedHeight(20);
        table.getDefaultCell().setBorder(Rectangle.BOTTOM);
        table.addCell(content);
        table.getDefaultCell().setHorizontalAlignment(Element.ALIGN_RIGHT);
        table.addCell("" + x + "/" + y);

        return table;
    }

    public static List<Distribution> getSubmissionDistribution(Submission submission) throws MetadataNotFoundException {

        List<Distribution> distributionList = new LinkedList<Distribution>();

        String filePath = submission.getFilePath();

        String student = submission.getOwner();

        int pages = PdfPageCount(filePath);

        String question = "";
        Distribution distribution = null;
        for (int i = 1; i <= pages; i++) {
            if (PdfMetadataQuery("page." + i, filePath).equals(question))
                distribution.setEndPage(i);
            else
            {
                if (distribution != null)
                    distributionList.add(distribution);

                question = PdfMetadataQuery("page." + i, filePath);

                distribution = new Distribution();

                distribution.setSubmissionId(submission.getId());
                distribution.setStartPage(i);
                distribution.setEndPage(i);
                distribution.setQuestion(question);
                distribution.setStudent(student);
            }
        }

        distributionList.add(distribution);

        return distributionList;
    }

    public static List<Distribution> getMarkedSubmissionDistribution(MarkedSubmission markedSubmission) throws MetadataNotFoundException {

        List<Distribution> distributionList = new LinkedList<Distribution>();

        String filePath = markedSubmission.getFilePath();

        int pages = PdfPageCount(filePath);

        String question = "";
        Distribution distribution = new Distribution();
        for (int i = 1; i <= pages; i++) {
            if (PdfMetadataQuery("page.question." + i, filePath).equals(question))
                distribution.setEndPage(i);
            else
            {
                distributionList.add(distribution);

                question = PdfMetadataQuery("page.question." + i, filePath);

                distribution = new Distribution();

                distribution.setSubmissionId(markedSubmission.getId());
                distribution.setStartPage(i);
                distribution.setEndPage(i);
                distribution.setQuestion(question);
                distribution.setStudent(PdfMetadataQuery("page.question." + i, filePath));
            }
        }

        distributionList.add(distribution);

        return distributionList;
    }

    /*
    Metadata table:
        Submission:
            "page.X" - question solved on page X

        MarkedSubmission:
            "page.user.X" - crsId of the solver from page X
            "page.question.X" - question solved on page X
     */

    /*
    The metadata query function takes a key value and the file path
    of the PDF and returns the value contained in the queried key or
    throws Exception MetadataNotFound otherwise.
    Done
     */
    public static String PdfMetadataQuery(String key, String filePath) throws MetadataNotFoundException {

        try {
            List<String> pageDetails;

            PdfReader reader = new PdfReader(filePath);

            Map <String, String> info = reader.getInfo();

            if (!info.containsKey(key))
                throw new MetadataNotFoundException();

            return info.get(key);
        } catch (IOException e) {
            return null;
        }
    }

    /*
    The injecter takes the pair (key, value) of strings and the path for
    the PDF and inserts metadata according to the input pair.
    Done
     */
    public static boolean PdfMetadataInject(String key, String value, String filePath) {
        try {
            String temp = "temp.pdf";

            FilesManip.fileMove(filePath, temp);

            PdfReader reader = new PdfReader(temp);
            PdfStamper stamper = new PdfStamper(reader, new FileOutputStream(filePath));

            Map <String, String> info = reader.getInfo();

            info.put(key, value);

            stamper.setMoreInfo(info);
            stamper.close();

            FilesManip.fileDelete(temp);

            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public static void PdfTakePages(String source, int start, int end, String destination) {
        try {
            Document document = new Document();

            PdfCopy copy = new PdfCopy(document, new FileOutputStream(destination));

            document.open();

            PdfReader reader = new PdfReader(source);

            for (int pn = start; pn <= end; )
                    copy.addPage(copy.getImportedPage(reader, pn++));

            document.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /*
    The merger takes a list of strings representing the file paths of the
    Pdf files with the destination file path and returns the page intervals
    which should be used as metadata.
    Done
     */
    public static List<String> PdfMerge(List<String> filePaths, String destination) {

        List<String> pageIntervals = new LinkedList<String>();
        try {
            Document document = new Document();

            PdfCopy copy = new PdfCopy(document, new FileOutputStream(destination));

            document.open();

            int actualPage = 1;
            for (String fileName : filePaths) {

                PdfReader reader = new PdfReader(fileName);

                int n = reader.getNumberOfPages();

                for (int pn = 0; pn < n; )
                    copy.addPage(copy.getImportedPage(reader, ++pn));

                pageIntervals.add(Integer.toString(actualPage) + "-" + Integer.toString(actualPage + n));
                actualPage += n;
            }

            document.close();
        } catch (Exception e) {
        }

        return pageIntervals;
    }

    /*
    The pageCount returns the number of pages of a pdf, given its path
    Done
    */
    public static int PdfPageCount(String filePath) {

        try {
            PdfReader reader = new PdfReader(filePath);

            return reader.getNumberOfPages();
        } catch (IOException e) {
            return 0;
        }
    }
}
