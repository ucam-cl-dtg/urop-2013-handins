package uk.ac.cam.sup.controllers;

import java.util.LinkedList;
import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

import org.hibernate.Session;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;

import uk.ac.cam.cl.dtg.teaching.hibernate.HibernateUtil;
import uk.ac.cam.sup.models.Bin;
import uk.ac.cam.sup.models.MarkedAnswer;
import uk.ac.cam.sup.models.ProposedQuestion;
import uk.ac.cam.sup.structures.Marking;
import uk.ac.cam.sup.tools.FilesManip;

import com.google.common.collect.ImmutableMap;

@Path("/bins")
public class AnnotatedAnswersController extends ApplicationController {

	/*
	 * Done
	 */
	@GET
	@Path("/{binId}/marked/download")
	@Produces("application/pdf")
	public Object downloadMarkedAnswers(@PathParam("binId") long binId) {

		// Set Hibernate and get user and bin
		Session session = HibernateUtil.getInstance().getSession();

		String user = getCurrentUser();

		Bin bin = (Bin) session.get(Bin.class, binId);

		// Check the existence of the bin
		if (bin == null)
			return Response.status(404).build();

		@SuppressWarnings("unchecked")
		List<MarkedAnswer> markedAnswers = session
				.createCriteria(MarkedAnswer.class, "marked")
				.createAlias("marked.answer", "answer")
				.add(Restrictions.eq("answer.bin", bin))
				.add(Restrictions.eq("owner", user)).list();

		List<Marking> markedList = new LinkedList<>();

		for (MarkedAnswer markedAnswer : markedAnswers)
			if (bin.canSeeAnnotated(user, markedAnswer)) {

				Marking marking = new Marking(markedAnswer.getFilePath());
				ProposedQuestion trash = new ProposedQuestion();

				marking.setOwner(getCurrentUser());
				marking.setQuestion(trash);
				markedList.add(marking);

			}
		return FilesManip.resultingFile(markedList);
	}

	@GET
	@Path("/{binId}/marked")
	@Produces("application/json")
	public Object viewMarkedAnswers(@PathParam("binId") long binId) {

		// Set Hibernate and get user and bin
		Session session = HibernateUtil.getInstance().getSession();

		String user = getCurrentUser();

		Bin bin = (Bin) session.get(Bin.class, binId);

		// Check the existence of the bin
		if (bin == null)
			return Response.status(404).build();

		// Voodoo
		@SuppressWarnings("unchecked")
		List<MarkedAnswer> res = (List<MarkedAnswer>) session
				.createCriteria(MarkedAnswer.class, "marked")
				.createAlias("marked.answer", "answer")
				.add(Restrictions.eq("owner", user))
				.add(Restrictions.eq("answer.bin", bin))
				.setProjection(
						Projections.distinct(Projections.property("annotator")))
				.list();

		return ImmutableMap.of("annotators", res, "bin", binId);
	}

	/*
	 * Done
	 */
	@GET
	@Path("/{binId}/marked/{markedAnswerId}/download")
	@Produces("application/pdf")
	public Object downloadMarkedAnswer(@PathParam("binId") long binId,
			@PathParam("markedAnswerId") long markedAnswerId) {

		// Set Hibernate and get user and bin
		Session session = HibernateUtil.getInstance().getSession();

		String user = getCurrentUser();

		Bin bin = (Bin) session.get(Bin.class, binId);

		// Check the existence of the bin
		if (bin == null)
			return Response.status(404).build();
		// TODO Security leak

		MarkedAnswer markedAnswer = (MarkedAnswer) session.get(
				MarkedAnswer.class, markedAnswerId);

		if (bin.canSeeAnnotated(user, markedAnswer)) {
			List<Marking> markedList = new LinkedList<>();

			markedList.add(new Marking(markedAnswer.getFilePath()));

			return FilesManip.resultingFile(markedList);
		}

		return Response
				.status(500)
				.entity(ImmutableMap.of("message",
						"File could not be processed.")).build();
	}
}
