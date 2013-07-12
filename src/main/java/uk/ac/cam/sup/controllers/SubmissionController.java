package uk.ac.cam.sup.controllers;

import com.google.common.collect.ImmutableMap;
import org.hibernate.Session;
import org.hibernate.criterion.CriteriaSpecification;
import org.hibernate.criterion.Restrictions;
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
import java.io.IOException;
import java.util.*;

import static uk.ac.cam.sup.tools.FilesManip.fileSave;
import static uk.ac.cam.sup.tools.PDFManip.PdfAddHeader;
import static uk.ac.cam.sup.tools.PDFManip.PdfMetadataInject;

@Path ("/submission/{binId}")
public class SubmissionController {

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
        if (!bin.canAddSubmission(user))
            return Response.status(401).build();

        // New submission and get id
        Submission submission = new Submission(user);
        session.save(submission);

        session.getTransaction().commit();
        session = HibernateUtil.getSession();
        session.beginTransaction();

        String directory = "temp/" + user + "/submissions/";
        String fileName = submission.getId() + ".pdf";

        try {
            fileSave(uploadForm.file, new File(directory + fileName));
        } catch (IOException e) {
            e.printStackTrace();

            return Response.status(500).build();
        }


        PdfMetadataInject("users", "1", directory + fileName);
        PdfMetadataInject("user.1", user, directory + fileName);
        PdfMetadataInject("bin", Long.toString(binId), directory + fileName);

        PdfAddHeader(directory + fileName, directory + "Headed" + fileName);

        // todo: convert the received file;

        // todo: split the received file;

        // todo: Inject file;

        submission.setBin(bin);
        submission.setUser(user);
        submission.setFilePath(directory + fileName);

        session.update(submission);

        return ImmutableMap.of("id", submission.getId(), "filepath", submission.getFilePath());
    }

    @GET
    @Produces("application/json")
    public Object listSubmissions(@PathParam("binId") long binId) {

        // Set Hibernate and get user
        Session session = HibernateUtil.getSession();

        String user = UserHelper.getCurrentUser();

        // Get Bin and check
        Bin bin = BinController.getBin(binId);

        if (bin == null)
            return Response.status(404).build();

        List<Submission> allSubmissions = session.createCriteria(Submission.class)
                                              .setResultTransformer(CriteriaSpecification.DISTINCT_ROOT_ENTITY)
                                              .add(Restrictions.eq("bin", bin))
                                              .list();
        List <Submission> accessibleSubmissions = new LinkedList<Submission>();

        for (Submission submission : allSubmissions)
            if (bin.canSeeSubmission(user, submission))
                accessibleSubmissions.add(submission);

        List<Map<String, String> > mapList = new LinkedList<Map<String, String>>();

        int p = 0;
        for (Submission submission : accessibleSubmissions) {

            Map<String, String> map = new HashMap<String, String>();

            map.put("id", Long.toString(submission.getId()));
            map.put("filePath" + p, submission.getFilePath());

            mapList.add(map);
        }

        return mapList;
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

        Submission submission = (Submission) session.get(Submission.class, submissionId);

        if (!bin.canSeeSubmission(user, submission))
            return Response.status(401).build();

        System.out.println(submission.getFilePath());

        return Response.ok(new File(submission.getFilePath())).build();
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

        Submission submission = (Submission) session.get(Submission.class, submissionId);

        if (!bin.canDeleteSubmission(user, submission))
            return Response.status(401).build();

        session.delete(submission);

        return Response.status(200).build();
    }
}
