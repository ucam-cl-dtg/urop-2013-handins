package uk.ac.cam.sup.tools;

import com.google.common.io.Files;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import org.apache.commons.lang3.RandomStringUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.LinkedList;
import java.util.Map;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PDFManip {
    // Fields
    private String filePath;

    public PDFManip(String filePath) throws Exception {
        setFilePath(filePath);
    }

    // FilePath
    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) throws Exception {
        File file = new File(filePath);

        if (!file.exists())
            //noinspection ResultOfMethodCallIgnored
            file.createNewFile();

        this.filePath = filePath;
    }

    // Actual useful functions

    /*
    FixMe: This function should be rewritten because it behaves badly in general
     */
   /* public void addHeader(String content) throws Exception {

        String randomTemp = "files/temp" + RandomStringUtils.randomAlphabetic(4) + ".pdf";

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
    }       */

    /*
    Metadata table:
        Submission:
            "pageOwnerX" - crsId of the solver of page X
            "pageQuestionX" - question solved on page X

     */

    /*
    The metadata query function takes a key value and the file path
    of the PDF and returns the value contained in the queried key or
    throws Exception MetadataNotFound otherwise.
    Done

     */
    public String queryMetadata(String key) throws Exception {

        PdfReader reader = new PdfReader(filePath);

        Map <String, String> info = reader.getInfo();

        if (info.containsKey(key))
            return info.get(key);
        else {
            List<String> lines = Files.readLines(new File(filePath), Charset.defaultCharset());

            String patternString = "(\\()" + "(\\w{1,9}?)" + "(\\))";
            Pattern pattern = Pattern.compile("(" + key + ")" + patternString);
            for (String line : lines) {
                Matcher matcher = pattern.matcher(line);

                if (matcher.find())
                    return matcher.group(3);
            }
        }

        throw new Exception();
    }

    /*
    The injecter takes the pair (key, value) of strings and the path for
    the PDF and inserts metadata according to the input pair.
    Done

     */
    public void injectMetadata(String key, String value) throws Exception {

        String randomTemp = "files/temp" + RandomStringUtils.randomAlphabetic(4) + ".pdf";

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
    Done

     */
    public void takePages(int start, int end, String destinationPath) throws Exception {

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
    public void add(String sourcePath) throws Exception {

        String randomTemp = "files/temp" + RandomStringUtils.randomAlphabetic(4) + ".pdf";

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

    /*
    Smart splitter / cropper!
     */
    public void takeBox(int pageIndex, float bottom, float top, String destinationPath) throws IOException, DocumentException {

        PdfReader reader = new PdfReader(filePath);
        Rectangle pageSize = reader.getPageSizeWithRotation(pageIndex);

        Document document = new Document();
        PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(destinationPath));

        writer.setBoxSize("crop", new Rectangle(0, bottom * pageSize.getHeight(), pageSize.getWidth(), top * pageSize.getHeight()));

        document.open();

        PdfContentByte content = writer.getDirectContent();
        PdfImportedPage page = writer.getImportedPage(reader, pageIndex);

        content.addTemplate(page, 0, 0);

        document.close();
    }

    public static void main(String[] args) throws Exception {
        PDFManip pdfManip = new PDFManip("2.pdf");

        List <String> lista = new LinkedList<String>();
        for (int i = 4; i >= 0; lista.add("e" + i + ".pdf"), i--)
            pdfManip.takeBox(4, 0.2f * i, 0.2f * (i + 1), "e" + i + ".pdf");

        FilesManip.mergePdf(lista, "eh.pdf");
    }
}
