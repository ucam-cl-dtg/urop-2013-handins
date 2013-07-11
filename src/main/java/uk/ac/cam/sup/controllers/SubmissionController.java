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

        Session session = HibernateUtil.getSession();
        System.out.println ("Muie");

        Submission submission = new Submission(UserHelper.getCurrentUser());

        if (!submission.getBin().canAddSubmission(UserHelper.getCurrentUser()))
            return Response.status(401).build();
        try {
            String fileName = UserHelper.getCurrentUser() + "_" + submission.getId();
            submission.setFilePath(fileName);

            File destinationFile = File.createTempFile(fileName, "." + "pdf", new File("temp/"));

            OutputStream op = new FileOutputStream(destinationFile);
            op.write(uploadForm.file);
            op.close();
        } catch (Exception e) {
                e.printStackTrace();
        }

        session.save(submission);

        return ImmutableMap.of("id", submission.getId());
    }

    @GET
    @Path("")
    @Produces("application/json")
    public Map<String, ?> listSubmissions(@FormParam("user") String user) {
        Session session = HibernateUtil.getSession();

        Submission bin = new Submission(user);
        session.save(bin);

        return ImmutableMap.of("id", bin.getId());
    }


}
