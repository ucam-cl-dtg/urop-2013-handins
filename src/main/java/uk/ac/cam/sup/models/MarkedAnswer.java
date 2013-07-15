package uk.ac.cam.sup.models;

import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;

@Entity
@Table(name = "MarkedAnswer")
public class MarkedAnswer {
    // Fields
    private long id;

    private String owner;
    private String filePath;
    private String question;

    private MarkedSubmission markedSubmission;
    private Answer answer;

    // Constructors
    public MarkedAnswer() {

    }

    // Id
    @Id
    @GeneratedValue(generator="increment")
    @GenericGenerator(name="increment", strategy="increment")
    public long getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
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

    // Submission
    @ManyToOne
    public MarkedSubmission getMarkedSubmission() {
        return markedSubmission;
    }

    public void setMarkedSubmission(MarkedSubmission markedSubmission) {
        this.markedSubmission = markedSubmission;
    }

    // Submission
    @ManyToOne
    public Answer getAnswer() {
        return answer;
    }

    public void setAnswer(Answer answer) {
        this.answer = answer;
    }
}
