package uk.ac.cam.sup.models;

import uk.ac.cam.sup.HibernateUtil;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.MappedSuperclass;
import javax.persistence.SequenceGenerator;
import javax.persistence.Transient;
import javax.validation.constraints.NotNull;

import org.hibernate.Session;

import uk.ac.cam.sup.structures.Distribution;
import uk.ac.cam.sup.tools.PDFManip;

@MappedSuperclass
public abstract class Submission<T> {
    // Fields
    @Id
    @GeneratedValue(strategy=GenerationType.SEQUENCE, generator="submissionIdSeq")
    @SequenceGenerator(name="submissionIdSeq", sequenceName="SUBMISSION_SEQ", allocationSize=1)
    private long id;

    private Date dateCreated;

    @NotNull
    private String owner;
    @NotNull
    private String filePath;

    @NotNull
    @ManyToOne
    private Bin bin;

    // Constructors
    public Submission() {
        dateCreated = new Date();
    }

    public Submission(String owner) {
        dateCreated = new Date();

        setOwner(owner);
    }

    // Id
    public long getId() {
        return id;
    }

    // Answers
    public abstract Set<T> getAllAnswers();
    public abstract void setAllAnswers(Set<T> answers);

    // Bin
    public Bin getBin() {
        return bin;
    }

    public void setBin(Bin bin) {
        this.bin = bin;
    }

    // FilePath
    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    // Owner
    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    // DateCreated
    public void setDateCreated(Date dateCreated) {
        this.dateCreated = dateCreated;
    }

    public Date getDateCreated() {
        return dateCreated;
    }

    // Actual useful functions

    /*

     */
    @Transient
    public abstract String getFolder();

    @Transient
    public abstract String getOriginalFilePath();

    /*

     */
    public List<Distribution> getSubmissionDistribution() {

        // Set Hibernate and get user
        Session session = HibernateUtil.getSession();

        List<Distribution> distributionList = new LinkedList<Distribution>();

        PDFManip pdfManip;
        try {
            pdfManip = new PDFManip(getFilePath());
        }
        catch (Exception e) {
            return null;
        }

        int pages = pdfManip.getPageCount();

        String prevStudent = null;
        ProposedQuestion prevQuestion = null;
        Distribution distribution = null;
        for (int i = 1; i <= pages; i++) {
            ProposedQuestion question;
            String student;
            try {
                question = (ProposedQuestion) session.get(ProposedQuestion.class, Long.parseLong(pdfManip.queryMetadata("pageQuestion" + i)));
                student = pdfManip.queryMetadata("pageOwner" + i);
            }
            catch (Exception e) {
                continue;
            }

            if (question == null || student == null)
                continue;

            if (prevQuestion != null && question.getId() == prevQuestion.getId() && student.equals(prevStudent))
                distribution.setEndPage(i);
            else {
                if (distribution != null)
                    distributionList.add(distribution);

                prevQuestion = question;
                prevStudent = student;

                distribution = new Distribution();

                distribution.setSubmission(this);
                distribution.setStartPage(i);
                distribution.setEndPage(i);
                distribution.setQuestion(question);
                distribution.setStudent(student);
            }
        }

        distributionList.add(distribution);

        return distributionList;
    }
}
