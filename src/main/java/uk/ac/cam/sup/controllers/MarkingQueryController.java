package uk.ac.cam.sup.controllers;

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

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import java.util.LinkedList;
import java.util.List;

@Path("/marking/bin/{binId}")
public class MarkingQueryController {
    @Context
    private HttpServletRequest request;

    /*
    Done

    Checked
     */
    private Object getQuery(long binId, Long questionId, String studentCrsId) {

        // Set Hibernate and get user and bin
        Session session = HibernateUtil.getSession();

        String user = UserHelper.getCurrentUser(request);

        Bin bin = (Bin) session.get(Bin.class, binId);

        // Check the existence of the bin
        if (bin == null)
            return Response.status(404).build();

        // Get all answers from the bin
        List<Answer> answers = new LinkedList<Answer>(bin.getAnswers());

        // Create the marking list for the file
        List<Marking> markingList = new LinkedList<Marking>();
        int actualPage = 1;
        for (Answer answer : answers)
            if (bin.canSeeAnswer(user, answer) &&
                    (questionId == null || answer.getQuestion().getId() == questionId) &&
                    (studentCrsId == null || answer.getOwner().equals(studentCrsId)))
            {
                Marking marking = new Marking();

                marking.setFilePath(answer.getFilePath());
                marking.setFirst(actualPage);

                try {
                    actualPage += new PDFManip(answer.getFilePath()).getPageCount();
                } catch (Exception e) {
                    continue;
                }
                marking.setLast(actualPage - 1);
                marking.setOwner(answer.getOwner());
                marking.setQuestion(answer.getQuestion());

                markingList.add(marking);
            }

        return FilesManip.resultingFile(markingList);

    }

    /*
    Done

    Checked
     */
    @GET
    @Path("/download")
    @Produces("application/pdf")
    public Object viewAll(@PathParam("binId") long binId) {

        return getQuery(binId, null, null);
    }

    /*
    Done

    Checked
     */
    @GET
    @Path("/student/{studentCrsId}/download")
    @Produces("application/pdf")
    public Object downloadStudentAnswers(@PathParam("binId") long binId,
                                         @PathParam("studentCrsId") String studentCrsId) {

        return getQuery(binId, null, studentCrsId);
    }

    /*
    Done
     */
    @GET
    @Path("/student/{studentCrsId}/question/{questionId}/download")
    @Produces("application/pdf")
    public Object downloadStudentQuestionAnswer(@PathParam("binId") long binId,
                                                @PathParam("studentCrsId") String studentCrsId,
                                                @PathParam("questionId}") long questionId) {

        return getQuery(binId, questionId, studentCrsId);
    }

    /*
    Done
     */
    @GET
    @Path("/question/{questionId}/download")
    @Produces("application/pdf")
    public Object getQuestion(@PathParam("binId") long binId,
                              @PathParam("questionId") long questionId) {


        return getQuery(binId, questionId, null);
    }

    /*
    Done
     */
    @GET
    @Path("/question/{questionId}/student/{studentCrsId}/download")
    @Produces("application/pdf")
    public Object downloadQuestionStudentAnswer(@PathParam("binId") long binId,
                                                @PathParam("questionId") long questionId,
                                                @PathParam("studentCrsId") String studentCrsId) {

        return getQuery(binId, questionId, studentCrsId);
    }
}