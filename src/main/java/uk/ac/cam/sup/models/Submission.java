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

    private Bin bin;

    private Set<Answer> answers;

    // Constructors
    public Submission() {

    }

    // Id
    @Id
    @GeneratedValue(generator="increment")
    @GenericGenerator(name="increment", strategy="increment")
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    // Answers
    @OneToMany(mappedBy="submission")
    public Set<Answer> getAnswers() {
        return answers;
    }

    public void setAnswers(Set answers) {
        this.answers = answers;
    }

    // Bin
    @ManyToOne
    public Bin getBin() {
        return bin;
    }

    public void setBin(Bin bin) {
        this.bin = bin;
    }

    // FilePath
    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    // User
    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }
}
