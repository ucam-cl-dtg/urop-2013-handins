package uk.ac.cam.sup.models;

import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;

@Entity
@Table(name = "Answer")
public class Answer {
    // Fields
    private String filePath;
    private String question;
    private int id;
    private Submission submission;
    private boolean finalState;

    // Constructors
    public Answer() {

    }

    public Answer(String q) {
        question = q;
    }

    // Id
    @Id
    @GeneratedValue(generator="increment")
    @GenericGenerator(name="increment", strategy="increment")
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
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

    public void setFilePath(String path) {
        this.filePath = filePath;
    }

    // Submission
    @ManyToOne
    public Submission getSubmission() {
       return submission;
    }

    public void setSubmission(Submission submission) {
      this.submission = submission;
    }

    // FinalState
    public boolean getFinalState() {
        return finalState;
    }

    public void setFinalState(boolean finalState) {
        this.finalState = finalState;
    }
}
