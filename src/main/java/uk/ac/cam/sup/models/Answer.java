package uk.ac.cam.sup.models;

import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;

@Entity
@Table(name = "Answer")
public class Answer {
    private String PDFpath;
    private String question;
    private int id;
    private Submission submission;

    Answer() {

    }

    Answer(String q) {
        question = q;
    }

    @Id
    @GeneratedValue(generator="increment")
    @GenericGenerator(name="increment", strategy="increment")
    public int getId() {
        return id;
    }

    public void setId(int i) {
        id = i;
    }

    public String getQuestion() {
        return question;
    }

    public void setQuestion(String q) {
        question = q;
    }

    public String getFilePath() {
        return PDFpath;
    }

    public void setFilePath(String path) {
        PDFpath = path;
    }

    @ManyToOne
    public Submission getSubmission() {
       return submission;
    }

    public void setSubmission(Submission submission) {
      this.submission = submission;
    }

}
