package uk.ac.cam.sup.models;

import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.util.Set;

@Entity
@Table(name = "Answer")
public class Answer {
    // Fields
    @Id
    @GeneratedValue(generator="increment")
    @GenericGenerator(name="increment", strategy="increment")
    private long id;

    private String owner;
    private String filePath;
    private String question;

    private boolean finalState;

    @ManyToOne
    private Submission submission;

    @ManyToOne
    private Bin bin;

    @OneToMany(mappedBy = "answer")
    private Set<MarkedAnswer> markedAnswers;

    // Constructors
    public Answer() {

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

    // Submission
    public Submission getSubmission() {
       return submission;
    }

    public void setSubmission(Submission submission) {
      this.submission = submission;
    }

    // MarkedAnswers
    public Set<MarkedAnswer> getMarkedAnswers() {
        return markedAnswers;
    }

    public void setMarkedAnswers(Set markedAnswers) {
        this.markedAnswers = markedAnswers;
    }

    // Bin
    public Bin getBin() {
        return bin;
    }

    public void setBin(Bin bin) {
        this.bin = bin;
    }

    // FinalState
    public boolean getFinalState() {
        return finalState;
    }

    public void setFinalState(boolean finalState) {
        this.finalState = finalState;
    }
}
