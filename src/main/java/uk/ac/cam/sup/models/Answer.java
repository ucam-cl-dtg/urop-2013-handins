package uk.ac.cam.sup.models;

import java.util.Date;
import java.util.Set;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

@Entity
@Table(name = "Answer")
public class Answer {
    // Fields
    @Id
    @GeneratedValue(strategy=GenerationType.SEQUENCE, generator="answerIdSeq")
    @SequenceGenerator(name="answerIdSeq", sequenceName="AnswerSEQ", allocationSize=1)
    private long id;

    private Date dateCreated;

    private String owner;
    private String filePath;

    private boolean last;
    private boolean downloaded;
    private boolean annotated;

    @ManyToOne
    private ProposedQuestion question;

    @ManyToOne
    private UnmarkedSubmission unmarkedSubmission;

    @ManyToOne
    private Bin bin;

    @OneToMany(mappedBy = "answer")
    private Set<MarkedAnswer> markedAnswers;

    // Constructors
    public Answer() {
        dateCreated = new Date();
    }

    // Id
    public long getId() {
        return id;
    }

    // Owner
    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    // Question
    public ProposedQuestion getQuestion() {
        return question;
    }

    public void setQuestion(ProposedQuestion question) {
        this.question = question;
    }

    // FilePath
    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    // UnmarkedSubmission
    public UnmarkedSubmission getUnmarkedSubmission() {
       return unmarkedSubmission;
    }

    public void setUnmarkedSubmission(UnmarkedSubmission unmarkedSubmission) {
      this.unmarkedSubmission = unmarkedSubmission;
    }

    // MarkedAnswers
    public Set<MarkedAnswer> getMarkedAnswers() {
        return markedAnswers;
    }

    public void setMarkedAnswers(Set<MarkedAnswer> markedAnswers) {
        this.markedAnswers = markedAnswers;

        if (markedAnswers.size() > 0)
            setAnnotated(true);
    }

    // Bin
    public Bin getBin() {
        return bin;
    }

    public void setBin(Bin bin) {
        this.bin = bin;
    }

    // FinalState
    public boolean isLast() {
        return last;
    }

    public void setLast(boolean last) {
        this.last = last;
    }

    // Annotated
    public boolean isAnnotated() {
        return annotated;
    }

    public void setAnnotated(boolean annotated) {
        this.annotated = annotated;
    }

    // DateCreated
    public void setDateCreated(Date dateCreated) {
        this.dateCreated = dateCreated;
    }

    public Date getDateCreated() {
        return dateCreated;
    }

    // Downloaded
    public boolean isDownloaded() {
        return downloaded;
    }

    public void setDownloaded(boolean downloaded) {
        this.downloaded = downloaded;
    }
}
