package uk.ac.cam.PDFTools;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import uk.ac.cam.sup.exceptions.MetadataNotFoundException;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class PDFManip {
    private static String root = "";

    public static PdfPTable getHeaderTable(int x, int y) {
        PdfPTable table = new PdfPTable(2);
        table.setTotalWidth(530);
        table.setLockedWidth(true);
        table.getDefaultCell().setFixedHeight(20);
        table.getDefaultCell().setBorder(Rectangle.BOTTOM);
        table.addCell("Student Name");
        table.getDefaultCell().setHorizontalAlignment(Element.ALIGN_RIGHT);
        table.addCell(String.format("Question X out of Y"));

        return table;
    }

    public static void main(String[] args) throws IOException, DocumentException {
/*
        String finalFileName = "q1.pdf";
        String filePath = "temp/";

        // Hibernating
        Session session = HibernateUtil.getSF().getCurrentSession();
        session.beginTransaction();

        Answer x1 = new Answer("q1");
        x1.setFilePath("q1.pdf");
        Answer x2 = new Answer("q2");
        x2.setFilePath("q2.pdf");

        session.save(x1);
        session.save(x2);

        session.getTransaction().commit();

        session = HibernateUtil.getSF().getCurrentSession();
        session.beginTransaction();

        List<Answer> pulaMea = session.createCriteria(Answer.class).list();

        session.getTransaction().commit();

        Document document = new Document();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        PdfCopy copy = new PdfCopy(document, baos);
        document.open();

        for (Answer p : pulaMea) {
            String fileName = p.getFilePath();

            PdfReader reader = new PdfReader(root + filePath + fileName);
            int n = reader.getNumberOfPages();

            for (int pn = 0; pn < n; )
                copy.addPage(copy.getImportedPage(reader, ++pn));
        }

        document.close();

        document = new Document();

        PdfReader reader = new PdfReader(baos.toByteArray());
        PdfStamper stamper = new PdfStamper(reader, new FileOutputStream(root + filePath + finalFileName));

        int n = reader.getNumberOfPages();
        for (int i = 1; i <= n; i++) {
            getHeaderTable(i, n).writeSelectedRows(
                        0, -1, 30, 795, stamper.getOverContent(i));
        }
        stamper.close();*/
    }

    /* The metadata query function takes a key value and the file path
     of the PDF and returns the value contained in the queried key or
     throws Exception MetadataNotFound otherwise. */
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

    /* The injecter takes the pair (key, value) of strings and the path for
     the PDF and inserts metadata according to the input pair. */
    public static boolean PdfMetadataInject(String key, String value, String filePath) {
        try {
            PdfReader reader = new PdfReader(filePath);
            PdfStamper stamper = new PdfStamper(reader, new FileOutputStream(filePath));

            Map <String, String> info = reader.getInfo();

            info.put(key, value);

            stamper.setMoreInfo(info);
            stamper.close();

            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /* The merger takes a list of strings representing the file paths of the
     Pdf files with the destination file path and returns the page intervals
     which should be used as metadata. */
    public static List<String> PdfMerge(String[] filePaths, String destination) {

        List<String> pageIntervals = new LinkedList<String>();
        try {
            Document document = new Document();
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

            PdfCopy copy = new PdfCopy(document, byteArrayOutputStream);

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
            e.printStackTrace();
        }

        return pageIntervals;
    }
}
