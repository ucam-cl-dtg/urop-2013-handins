package uk.ac.cam.sup.models;

import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.util.Set;

@Entity
@Table(name = "MarkedSubmission")
public class MarkedSubmission {
    // Fields
    private long id;

    private String owner;
    private String filePath;

    private Bin bin;

    private Set<MarkedAnswer> markedAnswers;

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

    // Owner
    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
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
    public Set<MarkedAnswer> getMarkedAnswers() {
        return markedAnswers;
    }

    public void setMarkedAnswers(Set markedAnswers) {
        this.markedAnswers = markedAnswers;
    }

    @ManyToOne
    public Bin getBin() {
        return bin;
    }

    public void setBin(Bin bin) {
        this.bin = bin;
    }
}
