package uk.ac.cam.sup.forms;

import javax.ws.rs.FormParam;

public class FileUploadForm {
    @FormParam("filename") public String filename;
    @FormParam("file") public byte[] file;
}