package uk.ac.cam.sup.tools;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import org.apache.commons.lang3.RandomStringUtils;
import uk.ac.cam.sup.exceptions.MetadataNotFoundException;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;

public class PDFManip {
    // Fields
    private String filePath;

    // Constructors
    public PDFManip() {

    }

    public PDFManip(String filePath) throws IOException {
        setFilePath(filePath);
    }

    // FilePath
    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) throws IOException {
        File file = new File(filePath);

        if (!file.exists())
            file.createNewFile();

        this.filePath = filePath;
    }

    // Actual useful functions

    /*
    FixMe: This function should be rewritten because it behaves badly in general
     */
    public void addHeader(String content) throws IOException, DocumentException {

        String randomTemp = "temp/temp" + RandomStringUtils.randomAlphabetic(4) + ".pdf";

        FilesManip.fileMove(filePath, randomTemp);

        PdfReader reader = new PdfReader(randomTemp);
        PdfStamper stamper = new PdfStamper(reader, new FileOutputStream(filePath));

        int n = reader.getNumberOfPages();
        for (int i = 1; i <= n; i++)
            getHeaderTable(i, n, content).writeSelectedRows(0, -1, 30, 795, stamper.getOverContent(i));

        stamper.close();

        FilesManip.fileDelete(randomTemp);
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
    /*
    Metadata table:
        Submission:
            "type" - "marked" / "unmarked"
            "page.owner.X" - crsId of the solver of page X
            "page.question.X" - question solved on page X

     */

    /*
    The metadata query function takes a key value and the file path
    of the PDF and returns the value contained in the queried key or
    throws Exception MetadataNotFound otherwise.
    Done
     */
    public String queryMetadata(String key) throws MetadataNotFoundException, IOException {

        List<String> pageDetails;

        PdfReader reader = new PdfReader(filePath);

        Map <String, String> info = reader.getInfo();

        if (!info.containsKey(key))
            throw new MetadataNotFoundException();

        return info.get(key);
    }

    /*
    The injecter takes the pair (key, value) of strings and the path for
    the PDF and inserts metadata according to the input pair.
    Done
     */
    public void injectMetadata(String key, String value) throws IOException, DocumentException {

        String randomTemp = "temp/temp" + RandomStringUtils.randomAlphabetic(4) + ".pdf";

        FilesManip.fileMove(filePath, randomTemp);

        PdfReader reader = new PdfReader(randomTemp);
        PdfStamper stamper = new PdfStamper(reader, new FileOutputStream(filePath));

        Map <String, String> info = reader.getInfo();

        info.put(key, value);

        stamper.setMoreInfo(info);
        stamper.close();

        FilesManip.fileDelete(randomTemp);
    }

    /*
    The function takes the interval of pages between start and end from the contained PDF
    and stores them in a new PDF created at the destinationPath.
     */
    public void takePages(int start, int end, String destinationPath) throws IOException, DocumentException {

        Document document = new Document();

        PdfCopy copy = new PdfCopy(document, new FileOutputStream(destinationPath));

        document.open();

        PdfReader reader = new PdfReader(filePath);

       for (int pn = start; pn <= end; )
           copy.addPage(copy.getImportedPage(reader, pn++));

        document.close();
    }

    /*
    The adder takes a new filePath of the source PDF which should be
    added at the end of the initial pdf
    Done
     */
    public void add(String sourcePath) throws IOException, DocumentException {

        String randomTemp = "temp/temp" + RandomStringUtils.randomAlphabetic(4) + ".pdf";

        Document document = new Document();

        PdfCopy copy = new PdfCopy(document, new FileOutputStream(randomTemp));

        document.open();

        PdfReader reader = new PdfReader(filePath);
        int n = reader.getNumberOfPages();

        for (int pn = 0; pn < n; )
            copy.addPage(copy.getImportedPage(reader, ++pn));

        reader = new PdfReader(sourcePath);
        n = reader.getNumberOfPages();

        for (int pn = 0; pn < n; )
            copy.addPage(copy.getImportedPage(reader, ++pn));

        document.close();

        FilesManip.fileMove(randomTemp, filePath);
    }

    /*
    The function returns the number of pages of the pdf whose path is stored.
    Done
    */
    public int getPageCount() {

        try {
            PdfReader reader = new PdfReader(filePath);

            return reader.getNumberOfPages();
        } catch (IOException e) {
            return 0;
        }
    }
}
