package uk.ac.cam.sup.models;

import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.util.Set;

@Entity
@Table(name = "Submission")
public class Submission {
    // Fields
    private long id;
    private String filePath;
    private String user;
    private Set<Answer> answers;

    // Constructors
    public Submission() {

    }

    public Submission(String user) {
        this.user = user;
    }

    @Id
    @GeneratedValue(generator="increment")
    @GenericGenerator(name="increment", strategy="increment")
    public long getId() {
        return id;
    }

    @OneToMany(mappedBy="submission")
    public Set<Answer> getAnswers(){ return answers; }

    public String getFilePath() { return filePath; }
    public String getUser() { return user; }


    //Setters
    public void setId(long id) { this.id = id; }
    public void setAnswers(Set<Answer> answers) {
        this.answers = answers;
    }
    public void setFilePath(String filePath) { this.filePath = filePath; }
    public void setUser(String user) { this.user = user; }



}
