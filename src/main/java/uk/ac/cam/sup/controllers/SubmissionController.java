package uk.ac.cam.sup.controllers;

import com.google.common.collect.ImmutableMap;
import org.hibernate.Session;
import org.jboss.resteasy.annotations.providers.multipart.MultipartForm;
import uk.ac.cam.sup.HibernateUtil;
import uk.ac.cam.sup.forms.FileUploadForm;
import uk.ac.cam.sup.helpers.UserHelper;
import uk.ac.cam.sup.models.Submission;

import javax.ws.rs.*;
import javax.ws.rs.core.Response;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.Map;

@Path ("/bin/{id}/submission")
public class SubmissionController {

    @POST
    @Path("")
    @Produces("application/json")
    @Consumes("application/pdf")
    public Object createSubmission(@MultipartForm FileUploadForm uploadForm) {

        // Get user
        String user = UserHelper.getCurrentUser();

        Submission submission = new Submission(user);

        if (!submission.getBin().canAddSubmission(user)
            return Response.status(401).build();

        saveFile(uploadForm.file, File.createTempFile(fileName, "." + "pdf", new File("temp/" + "submissions/" + user)));

        // TODO: convert the received file;

        // TODO: split the received file;

        try {
            String fileName = user + "_" + submission.getId();
            submission.setFilePath(fileName);
        } catch (Exception e) {
                e.printStackTrace();
        }

        // Start Hibernating
        Session session = HibernateUtil.getSession();

        session.save(submission);

        return ImmutableMap.of("id", submission.getId());
    }

    @GET
    @Path("")
    @Produces("application/json")
    public Map<String, ?> listSubmissions() {
        Session session = HibernateUtil.getSession();

        Submission bin = new Submission(user);
        session.save(bin);

        return ImmutableMap.of("id", bin.getId());
    }


}
