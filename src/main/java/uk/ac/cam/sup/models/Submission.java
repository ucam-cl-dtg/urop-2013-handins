package uk.ac.cam.sup.models;

import org.hibernate.Session;
import org.hibernate.annotations.GenericGenerator;
import uk.ac.cam.sup.HibernateUtil;
import uk.ac.cam.sup.exceptions.MetadataNotFoundException;
import uk.ac.cam.sup.structures.Distribution;
import uk.ac.cam.sup.tools.PDFManip;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

@MappedSuperclass
public abstract class Submission<T> {
    // Fields
    @Id
    @GeneratedValue(generator="increment")
    @GenericGenerator(name="increment", strategy="increment")
    private long id;

    @NotNull
    private String owner;
    @NotNull
    private String filePath;

    @NotNull
    @ManyToOne
    private Bin bin;

    // Constructors
    public Submission() {

    }

    public Submission(String owner) {
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

    // Actual useful functions

    /*

     */
    @Transient
    public abstract String getFolder();

    /*

     */
    public List<Distribution> getSubmissionDistribution() throws MetadataNotFoundException {

        // Set Hibernate and get user
        Session session = HibernateUtil.getSession();

        List<Distribution> distributionList = new LinkedList<Distribution>();

        PDFManip pdfManip = new PDFManip(getFilePath());

        int pages = pdfManip.getPageCount();

        ProposedQuestion prevQuestion = null;
        Distribution distribution = null;
        for (int i = 1; i <= pages; i++) {
            ProposedQuestion question = (ProposedQuestion) session.get(ProposedQuestion.class, Long.parseLong(pdfManip.queryMetadata("page.question." + i)));
            if (prevQuestion != null && question.getId() == prevQuestion.getId())
                distribution.setEndPage(i);
            else {
                if (distribution != null)
                    distributionList.add(distribution);

                prevQuestion = question;

                distribution = new Distribution();

                distribution.setSubmission(this);
                distribution.setStartPage(i);
                distribution.setEndPage(i);
                distribution.setQuestion(question);
                distribution.setStudent(pdfManip.queryMetadata("page.owner." + i));
            }
        }

        distributionList.add(distribution);

        return distributionList;
    }
}
