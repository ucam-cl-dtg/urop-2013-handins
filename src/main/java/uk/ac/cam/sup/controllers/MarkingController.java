package uk.ac.cam.sup.controllers;

import com.google.common.collect.ImmutableMap;
import org.hibernate.Session;
import org.hibernate.type.OrderedSetType;
import org.jboss.resteasy.annotations.providers.multipart.MultipartForm;
import uk.ac.cam.sup.HibernateUtil;
import uk.ac.cam.sup.forms.FileUploadForm;
import uk.ac.cam.sup.helpers.UserHelper;
import uk.ac.cam.sup.models.*;
import uk.ac.cam.sup.structures.Question;
import uk.ac.cam.sup.structures.Student;
import uk.ac.cam.sup.tools.FilesManip;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.*;

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

        Map<String, List<Question>> studentQuestions = new HashMap<String, List<Question>>();
        Set<String> studentSet = new TreeSet<String>();

        for (Answer answer : allAnswers) {

            if (bin.canSeeAnswer(user, answer))
            {
                if (!studentSet.contains(answer.getOwner())) {
                    studentQuestions.put(answer.getOwner(), new LinkedList<Question>());

                    studentSet.add(answer.getOwner());
                }

                boolean marked = answer.getMarkedAnswers().size() > 0;

                Question q = new Question(answer.getQuestion(), answer.getFilePath(), true, marked);

                studentQuestions.get(answer.getOwner()).add(q);
            }
        }

        List<Student> studentList = new LinkedList<Student>();

        for (String s : studentSet) {
            Student student = new Student();

            student.setName(s);
            student.setQuestions(studentQuestions.get(s));
            student.setMarked(student.getQuestions().size() == bin.getQuestionCount());

            studentList.add(student);
        }

        return ImmutableMap.of("studentList", studentList);
    }
}
