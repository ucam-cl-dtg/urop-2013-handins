package uk.ac.cam.sup.models;

import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name = "MarkedAnswer")
public class MarkedAnswer {
    // Fields
    @Id
    @GeneratedValue(strategy=GenerationType.SEQUENCE, generator="markedAnswerIdSeq")
    @SequenceGenerator(name="markedAnswerIdSeq", sequenceName="MarkedAnswerSEQ", allocationSize=1)
    private long id;

    private Date dateCreated;

    private String owner;
    private String filePath;
    private String annotator;

    @ManyToOne
    private MarkedSubmission markedSubmission;

    @ManyToOne
    private Answer answer;

    // Constructors
    public MarkedAnswer() {
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

    // FilePath
    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    // UnmarkedSubmission
    public MarkedSubmission getMarkedSubmission() {
        return markedSubmission;
    }

    public void setMarkedSubmission(MarkedSubmission markedSubmission) {
        this.markedSubmission = markedSubmission;
    }

    // UnmarkedSubmission
    public Answer getAnswer() {
        return answer;
    }

    public void setAnswer(Answer answer) {
        this.answer = answer;
    }

    // Annotator
    public String getAnnotator() {
        return annotator;
    }

    public void setAnnotator(String annotator) {
        this.annotator = annotator;
    }

    // DateCreated
    public void setDateCreated(Date dateCreated) {
        this.dateCreated = dateCreated;
    }

    public Date getDateCreated() {
        return dateCreated;
    }
}
