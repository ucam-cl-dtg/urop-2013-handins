package uk.ac.cam.sup.controllers;

import com.itextpdf.text.DocumentException;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;
import uk.ac.cam.sup.HibernateUtil;
import uk.ac.cam.sup.helpers.UserHelper;
import uk.ac.cam.sup.models.Answer;
import uk.ac.cam.sup.models.Bin;
import uk.ac.cam.sup.models.ProposedQuestion;
import uk.ac.cam.sup.structures.Marking;
import uk.ac.cam.sup.tools.FilesManip;
import uk.ac.cam.sup.tools.PDFManip;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

@Path("/marking/bin/{binId}")
public class MarkingQueryController {

    /*
    Done
     */
    @GET
    @Path("/download")
    @Produces("application/pdf")
    public Object viewAll(@PathParam("binId") long binId) throws IOException, DocumentException {

        // Set Hibernate and get user
        Session session = HibernateUtil.getSession();

        String user = UserHelper.getCurrentUser();

        // Get Bin and check
        Bin bin = BinController.getBin(binId);

        if (bin == null)
            return Response.status(401).build();

        @SuppressWarnings("unchecked")
        List<Answer> answers = session.createCriteria(Answer.class)
                .add(Restrictions.eq("bin", bin)).list();

        List<Marking> markingList = new LinkedList<Marking>();
        int actualPage = 1;
        for (Answer answer : answers)
            if (bin.canSeeAnswer(user, answer))
            {
                Marking marking = new Marking();

                marking.setFilePath(answer.getFilePath());
                marking.setFirst(actualPage);

                actualPage += new PDFManip(answer.getFilePath()).getPageCount();
                marking.setLast(actualPage - 1);
                marking.setOwner(answer.getOwner());
                marking.setQuestion(answer.getQuestion());

                markingList.add(marking);
            }

        return FilesManip.resultingFile(markingList);
    }

    /*
    Done
     */
    @GET
    @Path("/student/{studentCrsId}/download")
    @Produces("application/pdf")
    public Object downloadStudentAnswers(@PathParam("binId") long binId,
                                         @PathParam("studentCrsId") String studentCrsId) throws IOException, DocumentException {

        // Get user
        String user = UserHelper.getCurrentUser();

        // Get Bin and check
        Bin bin = BinController.getBin(binId);

        if (bin == null)
            return Response.status(404).build();

        List<Answer> answers = new LinkedList<Answer>(bin.getAnswers());

        List<Marking> markingList = new LinkedList<Marking>();
        int actualPage = 1;
        for (Answer answer : answers)
            if (answer.getOwner().equals(studentCrsId) && bin.canSeeAnswer(user, answer))
            {
                Marking marking = new Marking();

                marking.setFilePath(answer.getFilePath());
                marking.setFirst(actualPage);

                actualPage += new PDFManip(answer.getFilePath()).getPageCount();
                marking.setLast(actualPage - 1);
                marking.setOwner(studentCrsId);
                marking.setQuestion(answer.getQuestion());

                markingList.add(marking);
            }

        return FilesManip.resultingFile(markingList);
    }

    /*
    Done
     */
    @GET
    @Path("/student/{studentCrsId}/question/{questionId}/download")
    @Produces("application/pdf")
    public Object downloadStudentQuestionAnswer(@PathParam("binId") long binId,
                                                @PathParam("studentCrsId") String studentCrsId,
                                                @PathParam("questionId}") long questionId) throws IOException, DocumentException {

        // Set Hibernate and get user
        Session session = HibernateUtil.getSession();

        String user = UserHelper.getCurrentUser();

        // Get Bin and check
        Bin bin = BinController.getBin(binId);

        if (bin == null)
            return Response.status(401).build();

        if (session.createCriteria(Answer.class)
                   .add(Restrictions.eq("bin", bin))
                   .add(Restrictions.eq("question", session.get(ProposedQuestion.class, questionId)))
                   .add(Restrictions.eq("owner", studentCrsId))
                   .list().size() > 0) {
            Answer answer = (Answer) session.createCriteria(Answer.class)
                                            .add(Restrictions.eq("bin", bin))
                                            .add(Restrictions.eq("question", session.get(ProposedQuestion.class, questionId)))
                                            .add(Restrictions.eq("owner", studentCrsId))
                                            .list().get(0);

            if (!bin.canSeeAnswer(user, answer))
                return Response.status(401).build();

            List<Marking> markingList = new LinkedList<Marking>();
            Marking marking = new Marking();

            marking.setFilePath(answer.getFilePath());
            marking.setFirst(1);

            marking.setLast(new PDFManip(answer.getFilePath()).getPageCount());
            marking.setOwner(answer.getOwner());
            marking.setQuestion(answer.getQuestion());

            markingList.add(marking);

            return FilesManip.resultingFile(markingList);
        }

        return Response.status(404).build();
    }

    /*
    Done
     */
    @GET
    @Path("/question/{questionId}/download")
    @Produces("application/pdf")
    public Object getQuestion(@PathParam("binId") long binId, @PathParam("questionId") long questionId) throws IOException, DocumentException {

        // Set Hibernate and get user
        Session session = HibernateUtil.getSession();

        String user = UserHelper.getCurrentUser();

        // Get Bin and check
        Bin bin = BinController.getBin(binId);

        if (bin == null)
            return Response.status(401).build();

        @SuppressWarnings("unchecked")
        List<Answer> answers = session.createCriteria(Answer.class)
                .add(Restrictions.eq("bin", bin))
                .add(Restrictions.eq("question", session.get(ProposedQuestion.class, questionId)))
                .list();

        List<Marking> markingList = new LinkedList<Marking>();
        int actualPage = 1;
        for (Answer answer : answers)
            if (bin.canSeeAnswer(user, answer))
            {
                Marking marking = new Marking();

                marking.setFilePath(answer.getFilePath());
                marking.setFirst(actualPage);

                actualPage += new PDFManip(answer.getFilePath()).getPageCount();
                marking.setLast(actualPage - 1);
                marking.setOwner(answer.getOwner());
                marking.setQuestion(answer.getQuestion());

                markingList.add(marking);
            }

        return FilesManip.resultingFile(markingList);
    }

    /*
    Done
     */
    @GET
    @Path("/question/{questionId}/student/{studentCrsId}/download")
    @Produces("application/pdf")
    public Object downloadQuestionStudentAnswer(@PathParam("binId") long binId,
                                                @PathParam("questionId") long questionId,
                                                @PathParam("studentCrsId") String studentCrsId) throws IOException, DocumentException {

        return downloadStudentQuestionAnswer(binId, studentCrsId, questionId);
    }
}
