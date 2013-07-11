package uk.ac.cam.sup.controllers;

import com.google.common.collect.ImmutableMap;
import org.hibernate.Session;
import uk.ac.cam.sup.HibernateUtil;
import uk.ac.cam.sup.models.Bin;
import uk.ac.cam.sup.models.Submission;

import javax.ws.rs.FormParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import java.util.Map;

@Path ("/bin/{id}/submission")
public class SubmissionController {

    @POST
    @Path("")
    @Produces("application/json")
    public Map<String, ?> createBin(@FormParam("user") String user) {
        Session session = HibernateUtil.getSession();

        Submission bin = new Submission(owner, question);
        session.save(bin);

        return ImmutableMap.of("id", bin.getId());
    }

}
