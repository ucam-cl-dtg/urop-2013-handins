package uk.ac.cam.sup.controllers;

import com.google.common.collect.ImmutableMap;
import org.hibernate.Session;
import org.jboss.resteasy.annotations.providers.multipart.MultipartForm;
import uk.ac.cam.sup.HibernateUtil;
import uk.ac.cam.sup.forms.FileUploadForm;
import uk.ac.cam.sup.helpers.UserHelper;
import uk.ac.cam.sup.models.*;
import uk.ac.cam.sup.structures.Question;
import uk.ac.cam.sup.tools.FilesManip;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

@Path("/marking/{binId}")
public class MarkingController {

    @POST
    @Produces("application/json")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public Object createSubmission(@MultipartForm FileUploadForm uploadForm, @PathParam("binId") long binId) {

        // Set Hibernate and get user
        Session session = HibernateUtil.getSession();

        String user = UserHelper.getCurrentUser();

        // Get Bin and check
        Bin bin = BinController.getBin(binId);

        if (bin == null)
            return Response.status(404).build();
        if (!bin.canAddMarkedSubmission(user))
            return Response.status(401).build();

        // New submission and get id
        MarkedSubmission markedSubmission = new MarkedSubmission();
        session.save(markedSubmission);

        session.getTransaction().commit();

        // Restart session
        session = HibernateUtil.getSession();
        session.beginTransaction();

        String directory = "temp/" + user + "/submissions/annotated/";
        String fileName = "submission_" + markedSubmission.getId() + ".pdf";

        try {
            FilesManip.fileSave(uploadForm.file, directory + fileName);
        } catch (IOException e) {
            e.printStackTrace();

            return Response.status(500).build();
        }

        markedSubmission.setFilePath(directory + fileName);

        session.update(markedSubmission);

        return ImmutableMap.of("id", markedSubmission.getId(), "filePath", markedSubmission.getFilePath());
    }

    @GET
    @Produces("application/json")
    public Object viewMarkedSubmissions(@PathParam("binId") long binId) {

        // Set Hibernate and get user
        Session session = HibernateUtil.getSession();

        String user = UserHelper.getCurrentUser();

        // Get Bin and check
        Bin bin = BinController.getBin(binId);

        if (bin == null)
            return Response.status(404).build();

        List<Answer> allAnswers = new LinkedList<Answer>(bin.getAnswers());

        List<Object> accessibleAnswers = new LinkedList<Object>();

        for (Answer answer : allAnswers) {

            if (bin.canSeeAnswer(user, answer))
            {
                answer.getAnnotatedAnswers();

                Question q = new Question(answer.getQuestion(), answer.getFilePath(), true);
            }
        }

        List<Map<String, String>> mapList = new LinkedList<Map<String, String>>();

        int p = 0;
        for (Answer answer : accessibleAnswers) {

            Map<String, String> map = new HashMap<String, String>();

            map.put("id", Long.toString(answer.getId()));
            map.put("filePath" + p, answer.getFilePath());

            mapList.add(map);
        }

        return mapList;
    }
}
