package uk.ac.cam.tools;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class FilesManip {

    /*

     */
    public static void fileSave(byte[] data, File destinationFile) throws IOException {
        OutputStream outputStream = new FileOutputStream(destinationFile);

        outputStream.write(data);
        outputStream.close();
    }
}
