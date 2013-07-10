package uk.ac.cam.sup.models;

import org.hibernate.annotations.GenericGenerator;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "AnnotatedAnswer")
public class AnnotatedAnswer {
    // Fields
    private String PDFpath;
    private String question;
    private int id;

    // Class
    public AnnotatedAnswer() {

    }

    public AnnotatedAnswer(String q) {
        question = q;
    }

    // Id
    @Id
    @GeneratedValue(generator="increment")
    @GenericGenerator(name="increment", strategy="increment")
    public int getId() {
        return id;
    }

    public void setId(int i) {
        id = i;
    }

    // Question
    public String getQuestion() {
        return question;
    }

    public void setQuestion(String q) {
        question = q;
    }

    // FilePath
    public String getFilePath() {
        return PDFpath;
    }

    public void setFilePath(String path) {
        PDFpath = path;
    }
}