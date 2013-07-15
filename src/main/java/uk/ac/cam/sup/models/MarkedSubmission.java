package uk.ac.cam.sup.models;

import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.util.Set;

@Entity
@Table(name = "MarkedSubmission")
public class MarkedSubmission {
    // Fields
    private long id;

    private String user;
    private String filePath;

    private Bin bin;

    private Set<AnnotatedAnswer> annotatedAnswers;

    // Constructors
    public MarkedSubmission() {

    }

    // Id
    @Id
    @GeneratedValue(generator="increment")
    @GenericGenerator(name="increment", strategy="increment")
    public long getId() {
        return id;
    }

    public void setId(int i) {
        id = i;
    }

    // User
    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    // FilePath
    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    // Annotated answers
    @OneToMany(mappedBy = "markedSubmission")
    public Set<AnnotatedAnswer> getAnnotatedAnswers() {
        return annotatedAnswers;
    }

    public void setAnnotatedAnswers(Set annotatedAnswers) {
        this.annotatedAnswers = annotatedAnswers;
    }

    @ManyToOne
    public Bin getBin() {
        return bin;
    }

    public void setBin(Bin bin) {
        this.bin = bin;
    }
}
