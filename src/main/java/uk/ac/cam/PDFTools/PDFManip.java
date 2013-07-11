package uk.ac.cam.PDFTools;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import com.itextpdf.tool.xml.html.head.Title;
import org.apache.james.mime4j.field.datetime.DateTime;
import org.hibernate.Session;
import uk.ac.cam.sup.HibernateUtil;
import uk.ac.cam.sup.models.Answer;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.List;

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
        String finalFileName = "works.pdf";
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
        stamper.close();
    }
}
