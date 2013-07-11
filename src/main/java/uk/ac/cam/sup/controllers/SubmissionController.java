package uk.ac.cam.sup.controllers;

import com.google.common.collect.ImmutableMap;
import org.hibernate.Session;
import uk.ac.cam.sup.HibernateUtil;
import uk.ac.cam.sup.models.Bin;
import uk.ac.cam.sup.models.Submission;

import javax.ws.rs.*;
import java.util.Map;

@Path ("/bin/{id}/submission")
public class SubmissionController {

    @POST
    @Path("")
    @Produces("application/json")
    @Consumes("application/pdf")
    public Map<String, ?> createSubmission(@FormParam("user") String user) {

        Session session = HibernateUtil.getSession();

        Submission submission = new Submission(user);

        if (submission.getBin().canAddSubmission(user))
        {
            submission.setFilePath();
        }


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
