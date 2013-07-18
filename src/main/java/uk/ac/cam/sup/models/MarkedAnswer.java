package uk.ac.cam.sup.models;

import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;

@Entity
@Table(name = "MarkedAnswer")
public class MarkedAnswer {
    // Fields
    @Id
    @GeneratedValue(generator="increment")
    @GenericGenerator(name="increment", strategy="increment")
    private long id;

    private String owner;
    private String filePath;
    private String question;

    @ManyToOne
    private MarkedSubmission markedSubmission;

    @ManyToOne
    private Answer answer;

    // Constructors
    public MarkedAnswer() {

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
    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
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
}
