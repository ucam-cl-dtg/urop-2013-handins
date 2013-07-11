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
    private String filePath;
    private String question;
    private int id;

    // Class
    public AnnotatedAnswer() {

    }

    public AnnotatedAnswer(String question) {
        this.question = question;
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

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }
}
