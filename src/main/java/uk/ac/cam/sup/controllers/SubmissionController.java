package uk.ac.cam.sup.controllers;

import com.google.common.collect.ImmutableMap;
import org.hibernate.Session;
import org.jboss.resteasy.annotations.providers.multipart.MultipartForm;
import uk.ac.cam.sup.HibernateUtil;
import uk.ac.cam.sup.forms.FileUploadForm;
import uk.ac.cam.sup.helpers.UserHelper;
import uk.ac.cam.sup.models.Bin;
import uk.ac.cam.sup.models.UnmarkedSubmission;
import uk.ac.cam.sup.tools.FilesManip;
import uk.ac.cam.sup.tools.PDFManip;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.File;
import java.io.IOException;
import java.util.*;

@Path ("/submission/{binId}")
public class SubmissionController {

    @POST
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces("application/json")
    public Object createSubmission(@MultipartForm FileUploadForm uploadForm, @PathParam("binId") long binId) {

        // Set Hibernate and get user
        Session session = HibernateUtil.getSession();

        String user = UserHelper.getCurrentUser();

        // Get Bin and check
        Bin bin = BinController.getBin(binId);

        if (bin == null)
            return Response.status(404).build();
        if (!bin.canAddSubmission(user))
            return Response.status(401).build();

        // New unmarkedSubmission and get id
        UnmarkedSubmission unmarkedSubmission = new UnmarkedSubmission();
        session.save(unmarkedSubmission);

        session.getTransaction().commit();

        // Restart session
        session = HibernateUtil.getSession();
        session.beginTransaction();

        String directory = "temp/" + user + "/submissions/answers/";
        String fileName = "submission_" + unmarkedSubmission.getId() + ".pdf";

        try {
            FilesManip.fileSave(uploadForm.file, directory + fileName);
        } catch (IOException e) {
            e.printStackTrace();

            return Response.status(500).build();
        }

        unmarkedSubmission.setBin(bin);
        unmarkedSubmission.setOwner(user);
        unmarkedSubmission.setFilePath(directory + fileName);

        session.update(unmarkedSubmission);

        // todo: convert the received file;

        PDFManip pdfManip = new PDFManip(directory + fileName);
        pdfManip.injectMetadata("uploader", user);

        // ToDo: Redirect to splitting screen

        pdfManip.injectMetadata("page.owner.1", "ap760");
        pdfManip.injectMetadata("page.owner.2", "ap760");
        pdfManip.injectMetadata("page.owner.3", "ap760");
        pdfManip.injectMetadata("page.owner.4", "ap760");
        pdfManip.injectMetadata("page.owner.5", "ap760");
        pdfManip.injectMetadata("page.question.1", "qqq 2");
        pdfManip.injectMetadata("page.question.2", "qqq 2");
        pdfManip.injectMetadata("page.question.3", "EEEE");
        pdfManip.injectMetadata("page.question.4", "qqq 4");
        pdfManip.injectMetadata("page.question.5", "qqq 4");

        FilesManip.distributeSubmission(unmarkedSubmission);

        return ImmutableMap.of("unmarkedSubmission", ImmutableMap.of("id", unmarkedSubmission.getId(),
                                                             "link", unmarkedSubmission.getLink(),
                                                             "filePath", unmarkedSubmission.getFilePath()));
    }

    @GET
    @Produces("application/json")
    public Object listSubmissions(@PathParam("binId") long binId) {

        // Get user
        String user = UserHelper.getCurrentUser();

        // Get Bin and check
        Bin bin = BinController.getBin(binId);

        if (bin == null)
            return Response.status(404).build();

        List<UnmarkedSubmission> allUnmarkedSubmissions = new LinkedList<UnmarkedSubmission>(bin.getUnmarkedSubmissions());
        List<UnmarkedSubmission> accessibleUnmarkedSubmissions = new LinkedList<UnmarkedSubmission>();

        for (UnmarkedSubmission unmarkedSubmission : allUnmarkedSubmissions)
            if (bin.canSeeSubmission(user, unmarkedSubmission))
                accessibleUnmarkedSubmissions.add(unmarkedSubmission);

        List<Map<String, String> > mapList = new LinkedList<Map<String, String>>();

        int p = 0;
        for (UnmarkedSubmission unmarkedSubmission : accessibleUnmarkedSubmissions) {

            Map<String, String> map = new HashMap<String, String>();

            map.put("id", Long.toString(unmarkedSubmission.getId()));
            map.put("filePath", unmarkedSubmission.getFilePath());
            map.put("link", unmarkedSubmission.getLink());

            mapList.add(map);
        }

        return ImmutableMap.of("submissions", mapList);
    }

    @GET
    @Path("/{submissionId}")
    @Produces("application/pdf")
    public Object seeSubmission(@PathParam("binId") long binId, @PathParam("submissionId") long submissionId) {

        // Set Hibernate and get user
        Session session = HibernateUtil.getSession();

        String user = UserHelper.getCurrentUser();

        // Get Bin and check
        Bin bin = BinController.getBin(binId);

        if (bin == null)
            return Response.status(404).build();

        UnmarkedSubmission unmarkedSubmission = (UnmarkedSubmission) session.get(UnmarkedSubmission.class, submissionId);

        if (!bin.canSeeSubmission(user, unmarkedSubmission))
            return Response.status(401).build();

        return Response.ok(new File(unmarkedSubmission.getFilePath())).build();
    }

    @DELETE
    @Path("/{submissionId}")
    @Produces("application/json")
    public Object deleteSubmission(@PathParam("binId") long binId, @PathParam("submissionId") long submissionId) {

        // Set Hibernate and get user
        Session session = HibernateUtil.getSession();

        String user = UserHelper.getCurrentUser();

        // Get Bin and check
        Bin bin = BinController.getBin(binId);

        if (bin == null)
            return Response.status(404).build();

        UnmarkedSubmission unmarkedSubmission = (UnmarkedSubmission) session.get(UnmarkedSubmission.class, submissionId);

        if (session != null)
        {
            if (!bin.canDeleteSubmission(user, unmarkedSubmission))
                return Response.status(401).build();

            session.delete(unmarkedSubmission);
        }

        return Response.status(200).build();
    }
}
