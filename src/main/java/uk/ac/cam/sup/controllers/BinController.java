package uk.ac.cam.sup.controllers;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.hibernate.Session;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.jboss.resteasy.annotations.Form;
import org.jboss.resteasy.annotations.providers.multipart.MultipartForm;

import uk.ac.cam.cl.dtg.teaching.api.DashboardApi.DashboardApiWrapper;
import uk.ac.cam.cl.dtg.teaching.hibernate.HibernateUtil;
import uk.ac.cam.sup.forms.FileUploadForm;
import uk.ac.cam.sup.forms.SplittingForm;
import uk.ac.cam.sup.models.Answer;
import uk.ac.cam.sup.models.Bin;
import uk.ac.cam.sup.models.BinAccessPermission;
import uk.ac.cam.sup.models.BinUserMarkingPermission;
import uk.ac.cam.sup.models.ProposedQuestion;
import uk.ac.cam.sup.models.UnmarkedSubmission;
import uk.ac.cam.sup.tools.FilesManip;
import uk.ac.cam.sup.tools.PDFManip;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

// Documented

@Path("/bins")
@Produces("application/json")
public class BinController extends ApplicationController {

	@GET
	@Path("/create")
	public Object viewForCreateBin() {
		return Response.ok().build();
	}

	/*
	 * Done
	 * 
	 * Checked
	 */
	@POST
	public Object addBin(@FormParam("name") String name,
			@FormParam("owner") String owner) {

		// Sanity Checking
		name = name.trim();

		if (name == null || name.isEmpty())
			return Response.status(400)
					.entity(ImmutableMap.of("message", "Unacceptable name."))
					.build();

		// Set Hibernate and get user
		Session session = HibernateUtil.getInstance().getSession();

		String user = getCurrentUser();
		if (owner == null)
			owner = user;

		// Create a new bin
		Bin bin = new Bin(owner, name.trim());

		// Save and return bin details
		session.save(bin);

		// Add owner to permissions
		try {
			session.save(new BinUserMarkingPermission(bin, owner));
			session.save(new BinAccessPermission(bin, owner,
					new DashboardApiWrapper(getDashboardUrl(), getApiKey())));
		} catch (Exception e) {
			return Response
					.status(202)
					.entity(ImmutableMap.of("id", bin.getId(), "name",
							bin.getName())).build();
		}

		return bin.toJSON();
	}

	/*
	 * Done
	 * 
	 * Checked
	 */
	@GET
	@Path("/{binId}")
	public Object viewBin(@PathParam("binId") long binId) {

		// Set Hibernate and get user and bin
		Session session = HibernateUtil.getInstance().getSession();

		String user = getCurrentUser();

		Bin bin = (Bin) session.get(Bin.class, binId);

		// Check the existence of the bin
		if (bin == null)
			return Response.status(404).build();

		if (!bin.canUploadIntoBin(user))
			return Response.status(403)
					.entity(ImmutableMap.of("message", "Cannot see bin."))
					.build();

		// Return bin details
		return ImmutableMap.of("bin", bin.toJSON());
	}

	/*
	 * Done
	 * 
	 * Checked
	 */
	@POST
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	@Produces("application/json")
	@Path("/{binId}")
	public Object addSubmission(@MultipartForm FileUploadForm uploadForm,
			@PathParam("binId") long binId) {

		// Sanity check
		if (!uploadForm.validate())
			return Response.status(400)
					.entity(ImmutableMap.of("message", "File not found."))
					.build();

		// Set Hibernate and get user and bin
		Session session = HibernateUtil.getInstance().getSession();

		String user = getCurrentUser();

		Bin bin = (Bin) session.get(Bin.class, binId);

		// Check the existence of the bin
		if (bin == null)
			return Response.status(404).build();

		if (!bin.canAddSubmission(user))
			return Response
					.status(403)
					.entity(ImmutableMap.of("message",
							"Cannot upload into bin.")).build();

		// Create directory
		String tempDirectory = FilesManip.newDirectory("files/" + user
				+ "/submissions/temp/", false);
		String directory = FilesManip.newDirectory("files/" + user
				+ "/submissions/answers/", false);

		// Save the submission
		String randomTemp = "temp" + RandomStringUtils.randomAlphabetic(4);
		try {
			FilesManip.fileSave(uploadForm.file, tempDirectory + randomTemp);
		} catch (Exception e) {
			return Response.status(500)
					.entity(ImmutableMap.of("message", "Unable to save file."))
					.build();
		}

		// New unmarkedSubmission to get id
		UnmarkedSubmission unmarkedSubmission = new UnmarkedSubmission();
		session.save(unmarkedSubmission);

		String fileName = "submission_" + unmarkedSubmission.getId() + ".pdf";

		try {
			FilesManip.manage(tempDirectory, randomTemp, directory + fileName);
		} catch (Exception e) {
			return Response.status(500).entity(
					ImmutableMap.of("message", "Unable to manage the file."));
		}

		// Add the submission to the database
		unmarkedSubmission.setFilePath(directory + fileName);
		unmarkedSubmission.setBin(bin);
		unmarkedSubmission.setOwner(user);

		session.update(unmarkedSubmission);

		FilesManip.deleteFolder(new File(FilesManip.newDirectory("files/"
				+ user + "/submissions/temp/", false)));

		return ImmutableMap.of("unmarkedSubmission",
				ImmutableMap.of("id", unmarkedSubmission.getId()), "bin",
				bin.getId());
	}

	/*
	 * Done
	 * 
	 * Checked
	 */
	@GET
	@Path("/{binId}/permissions/")
	public Object viewBinPermissionsList(@PathParam("binId") long binId) {

		// Set Hibernate and get user and bin
		Session session = HibernateUtil.getInstance().getSession();

		String user = getCurrentUser();

		Bin bin = (Bin) session.get(Bin.class, binId);

		// Check the existence of the bin
		if (bin == null)
			return Response.status(404).build();

		if (!bin.hasTotalAccess(user))
			return Response
					.status(403)
					.entity(ImmutableMap.of("message",
							"Unable to access details.")).build();

		// Get the list of people who can access the bin
		@SuppressWarnings("unchecked")
		List<BinAccessPermission> permissions = session
				.createCriteria(BinAccessPermission.class)
				.add(Restrictions.eq("bin", bin))
				.addOrder(Order.asc("userCrsId")).list();

		// Create list of people who have access to the bin
		List<String> res = new LinkedList<>();
		for (BinAccessPermission binPermission : permissions)
			res.add(binPermission.getUserCrsId());

		return ImmutableMap.of("users", res);
	}

	/*
	 * Done
	 * 
	 * Checked
	 */
	@GET
	@Path("/{binId}/submissions")
	public Object viewSubmissionList(@PathParam("binId") long binId) {

		// Set Hibernate and get user and bin
		Session session = HibernateUtil.getInstance().getSession();

		String user = getCurrentUser();

		Bin bin = (Bin) session.get(Bin.class, binId);

		// Check the existence of the bin
		if (bin == null)
			return Response.status(404).build();

		if (!bin.canUploadIntoBin(user))
			return Response.status(403)
					.entity(ImmutableMap.of("message", "Cannot see bin."))
					.build();

		// Get all submissions from the list
		// noinspection unchecked
		@SuppressWarnings("unchecked")
		List<UnmarkedSubmission> allUnmarkedSubmissions = (List<UnmarkedSubmission>) session
				.createCriteria(UnmarkedSubmission.class)
				.add(Restrictions.eq("bin", bin))
				.add(Restrictions.eq("owner", user)).addOrder(Order.asc("id"))
				.list();

		// Filter all visible submissions and get their link and Id
		List<ImmutableMap<String, ?>> mapList = new LinkedList<>();
		for (UnmarkedSubmission unmarkedSubmission : allUnmarkedSubmissions)
			if (bin.canSeeSubmission(user, unmarkedSubmission))
				mapList.add(ImmutableMap.of("link", unmarkedSubmission.getId(),
						"id", Long.toString(unmarkedSubmission.getId())));

		return ImmutableMap.of("submissions", mapList);
	}

	/*
	 * Done
	 * 
	 * Checked
	 */
	@POST
	@Path("/{binId}/submissions/{submissionId}")
	public Object splitSubmission(@PathParam("binId") long binId,
			@PathParam("submissionId") long submissionId,
			@Form SplittingForm split) {

		// Set Hibernate and get user
		Session session = HibernateUtil.getInstance().getSession();

		String user = getCurrentUser();

		Bin bin = (Bin) session.get(Bin.class, binId);

		// Check the existence of the bin
		if (bin == null)
			return Response.status(404).build();

		if (!bin.canAddSubmission(user))
			return Response
					.status(403)
					.entity(ImmutableMap.of("message",
							"Cannot upload into bin.")).build();

		// Get the unmarkedSubmission and reset the path
		UnmarkedSubmission unmarkedSubmission = (UnmarkedSubmission) session
				.get(UnmarkedSubmission.class, submissionId);

		unmarkedSubmission.setSplitFilePath(null);

		// Check the existence and validity of the submission
		if (!unmarkedSubmission.getOwner().equals(user))
			return Response
					.status(403)
					.entity(ImmutableMap.of("message",
							"Unable to split the submission.")).build();

		for (Answer answer : unmarkedSubmission.getAllAnswers())
			if (answer.isDownloaded())
				return Response
						.status(403)
						.entity(ImmutableMap.of("message",
								"Cannot upload answer to "
										+ answer.getQuestion().getName()
										+ ". Already downloaded for marking."))
						.build();

		// Sanity check
		if (!split.validate(unmarkedSubmission))
			return Response.status(400)
					.entity(ImmutableMap.of("message", "Unacceptable split."))
					.build();

		// Delete all answers from the submission
		for (Answer answer : unmarkedSubmission.getAllAnswers()) {

			FilesManip.fileDelete(answer.getFilePath());
			session.delete(answer);

			if (answer.isLast()) {
				@SuppressWarnings("unchecked")
				List<Answer> altAnswers = session.createCriteria(Answer.class)
						.add(Restrictions.eq("bin", answer.getBin()))
						.add(Restrictions.eq("owner", answer.getOwner()))
						.add(Restrictions.eq("question", answer.getQuestion()))
						.addOrder(Order.desc("dateCreated")).list();

				if (altAnswers.size() > 0)
					altAnswers.get(0).setLast(true);
			}
		}

		if (split.elements() == 0)
			return Response.ok().build();

		// Inject the pdf with the metadata needed to split it
		PDFManip pdfManip;
		try {
			pdfManip = new PDFManip(unmarkedSubmission.getOriginalFilePath());
		} catch (Exception e) {
			return Response.status(500)
					.entity(ImmutableMap.of("message", "Unable to save."))
					.build();
		}

		// Create directory
		String directory = FilesManip.newDirectory("files/" + user
				+ "/submissions/temp/", false);

		// Split questions
		List<Integer> startPageFinal = new LinkedList<>();
		List<Integer> endPageFinal = new LinkedList<>();
		List<String> pathList = new LinkedList<>();

		// Adding the new File
		String splitName = unmarkedSubmission.getFilePath();
		splitName = FilenameUtils.removeExtension(splitName);
		unmarkedSubmission.setSplitFilePath(splitName + "a.pdf");

		int actualPage = 0;
		try {
			for (int i = 0; i < split.elements(); pathList.add(directory
					+ "file" + i + ".pdf"), i++) {
				if (split.getStartPage(i) == split.getEndPage(i)) {

					pdfManip.takeBox(split.getStartPage(i), split.getEndLoc(i),
							split.getStartLoc(i), directory + "file" + i
									+ ".pdf");

					actualPage++;
					startPageFinal.add(actualPage);
					endPageFinal.add(actualPage);
				} else {
					pdfManip.takeBox(split.getStartPage(i), 0,
							split.getStartLoc(i), directory + "t1.pdf");
					if (split.getStartPage(i) + 1 != split.getEndPage(i))
						pdfManip.takePages(split.getStartPage(i) + 1,
								split.getEndPage(i) - 1, directory + "t2.pdf");
					pdfManip.takeBox(split.getEndPage(i), split.getEndLoc(i),
							1f, directory + "t3.pdf");

					if (split.getStartPage(i) + 1 != split.getEndPage(i))
						FilesManip.mergePdf(ImmutableList.of(directory
								+ "t1.pdf", directory + "t2.pdf", directory
								+ "t3.pdf"), directory + "file" + i + ".pdf");
					else
						FilesManip.mergePdf(ImmutableList.of(directory
								+ "t1.pdf", directory + "t3.pdf"), directory
								+ "file" + i + ".pdf");

					actualPage++;
					startPageFinal.add(actualPage);
					actualPage += (new PDFManip(directory + "file" + i + ".pdf"))
							.getPageCount() - 1;
					endPageFinal.add(actualPage);
				}
			}

			FilesManip.mergePdf(pathList, unmarkedSubmission.getFilePath());
			pdfManip.setFilePath(unmarkedSubmission.getFilePath());

			// Mark simply
			for (int i = 0; i < split.elements(); i++)
				FilesManip.markPdf(pdfManip, user, (ProposedQuestion) session
						.get(ProposedQuestion.class, split.getQuestionId(i)),
						startPageFinal.get(i), endPageFinal.get(i));

			// Split the resulting pdf
			FilesManip.distributeSubmission(user, unmarkedSubmission);
		} catch (Exception e) {
			return Response.status(500)
					.entity(ImmutableMap.of("message", "Unable to save."))
					.build();
		}

		FilesManip.deleteFolder(new File(FilesManip.newDirectory("files/"
				+ user + "/submissions/temp", false)));

		return Response.ok().build();
	}

	/*
	 * Done
	 * 
	 * Checked
	 */
	@GET
	@Path("/{binId}/questions")
	public Object viewBinQuestions(@PathParam("binId") long binId) {

		// Set Hibernate and get user
		Session session = HibernateUtil.getInstance().getSession();

		String user = getCurrentUser();

		Bin bin = (Bin) session.get(Bin.class, binId);
		// Check the existence of the bin
		if (bin == null)
			return Response.status(404).build();

		if (!bin.canUploadIntoBin(user))
			return Response
					.status(403)
					.entity(ImmutableMap.of("message",
							"Cannot upload into bin.")).build();

		// Query for all the questions in the bin
		@SuppressWarnings("unchecked")
		List<ProposedQuestion> questions = session
				.createCriteria(ProposedQuestion.class)
				.add(Restrictions.eq("bin", bin)).addOrder(Order.asc("id"))
				.list();

		// Create the list of questions as json
		List<ImmutableMap<String,?>> result = new LinkedList<ImmutableMap<String,?>>();
		for (ProposedQuestion question : questions) {
			result.add(question.toJSON());
		}

		return ImmutableMap.of("questions", result);
	}
}
