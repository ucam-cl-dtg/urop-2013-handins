package uk.ac.cam.PDFTools;

import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.PdfCopy;
import com.itextpdf.text.pdf.PdfReader;
import org.hibernate.Session;
import uk.ac.cam.sup.HibernateUtil;
import uk.ac.cam.sup.models.Answer;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

public class PDFManip {
    private static String root = "";

    public static void main(String[] args) throws IOException, DocumentException {
        String finalFileName = "works.pdf";
        String filePath = "temp/";

        Document document = new Document();

        PdfCopy copy = new PdfCopy(document, new FileOutputStream(root + filePath + finalFileName));
        document.open();

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

        System.out.println(pulaMea.size());

        for (Answer p : pulaMea) {
            String fileName = p.getFilePath();
            System.out.println(root + filePath+fileName);

            PdfReader reader = new PdfReader(root + filePath + fileName);
            int n = reader.getNumberOfPages();

            for (int page = 0; page < n; page++)
                copy.addPage(copy.getImportedPage(reader, page));
        }
        document.close();
    }
}
