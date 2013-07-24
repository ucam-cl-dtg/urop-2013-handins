package uk.ac.cam.sup.controllers;

import com.itextpdf.text.DocumentException;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;
import uk.ac.cam.sup.HibernateUtil;
import uk.ac.cam.sup.helpers.UserHelper;
import uk.ac.cam.sup.models.Bin;
import uk.ac.cam.sup.models.MarkedAnswer;
import uk.ac.cam.sup.structures.Marking;
import uk.ac.cam.sup.tools.FilesManip;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

public class AnnotatedAnswersController {

    /*
    Done
     */
    @GET
    @Path("/{binId}/marked/{markedAnswerId}/download")
    @Produces("application/pdf")
    public Object downloadMarkedAnswer(@PathParam("binId") long binId, @PathParam("markedAnswerId") long markedAnswerId) throws IOException, DocumentException {

        // Set Hibernate and get user
        Session session = HibernateUtil.getSession();

        String user = UserHelper.getCurrentUser();

        Bin bin = (Bin) session.get(Bin.class, binId);

        MarkedAnswer markedAnswer = (MarkedAnswer) session.get(MarkedAnswer.class, markedAnswerId);

        if (bin.canSeeAnnotated(user, markedAnswer)) {
            List<Marking> markedList = new LinkedList<Marking>();

            markedList.add(new Marking(markedAnswer.getFilePath()));

            return FilesManip.resultingFile(markedList);
        }

        return Response.status(401).build();
    }

    /*
    Done
     */
    @GET
    @Path("/{binId}/marked/download")
    @Produces("application/pdf")
    public Object downloadMarkedAnswers(@PathParam("binId") long binId) throws IOException, DocumentException {

        // Set Hibernate and get user
        Session session = HibernateUtil.getSession();

        String user = UserHelper.getCurrentUser();

        Bin bin = (Bin) session.get(Bin.class, binId);

        @SuppressWarnings("unchecked")
        List<MarkedAnswer> markedAnswers = session.createCriteria(MarkedAnswer.class)
                .add(Restrictions.eq("bin", bin))
                .add(Restrictions.eq("owner", user))
                .list();

        List<Marking> markedList = new LinkedList<Marking>();


        for (MarkedAnswer markedAnswer : markedAnswers)
            if (bin.canSeeAnnotated(user, markedAnswer)) {

                markedList.add(new Marking(markedAnswer.getFilePath()));

                return FilesManip.resultingFile(markedList);
            }

        return Response.status(401).build();
    }

}
