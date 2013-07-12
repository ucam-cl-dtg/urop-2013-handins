package uk.ac.cam.sup.controllers;

import com.google.common.collect.ImmutableMap;
import org.hibernate.Session;
import org.jboss.resteasy.annotations.providers.multipart.MultipartForm;
import uk.ac.cam.sup.HibernateUtil;
import uk.ac.cam.sup.forms.FileUploadForm;
import uk.ac.cam.sup.helpers.UserHelper;
import uk.ac.cam.sup.models.Bin;
import uk.ac.cam.sup.models.Submission;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;

import static uk.ac.cam.sup.tools.FilesManip.fileSave;

@Path ("/bin/{id}/submission")
public class SubmissionController {

    @POST
    @Path("")
    @Produces("application/json")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public Object createSubmission(@MultipartForm FileUploadForm uploadForm, @PathParam("id") long id) {

        // Get user
        String user = UserHelper.getCurrentUser();

        Submission submission = new Submission(user);

        submission.setBin(BinController.getBin(id));

        // Start Hibernating
        Session session = HibernateUtil.getSession();

        session.save(submission);
        session.getTransaction().commit();
        session = HibernateUtil.getSession();
        session.beginTransaction();

        System.out.println(Long.toString(submission.getId()));
        System.out.println("." + "pdf");

        if (!submission.getBin().canAddSubmission(user))
            return Response.status(401).build();

        try {
            fileSave(uploadForm.file, new File("temp/" + user + "/submissions/" + submission.getId() + ".pdf"));
        } catch (IOException e) {
            // Didn't save?
        }

        // todo: convert the received file;

        // todo: split the received file;

        // todo: Inject file;

        submission.setFilePath("temp/" + user + "submission/" + submission.getId() + ".pdf");


        session.save(submission);

        return ImmutableMap.of("id", submission.getId(), "filepath", submission.getFilePath());
    }
    /*
    @GET
    @Path("")
    @Produces("application/json")
    public Map<String, ?> listSubmissions() {
        Session session = HibernateUtil.getSession();

        Submission bin = new Submission();
        session.save(bin);

        return ImmutableMap.of("id", bin.getId());
    } */
}
