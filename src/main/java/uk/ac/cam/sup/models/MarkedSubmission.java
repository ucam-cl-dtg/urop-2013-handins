package uk.ac.cam.sup.models;

import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.util.Set;

@Entity
@Table(name = "MarkedSubmission")
public class MarkedSubmission {
    // Fields
    @Id
    @GeneratedValue(generator="increment")
    @GenericGenerator(name="increment", strategy="increment")
    private long id;

    private String owner;
    private String filePath;

    @ManyToOne
    private Bin bin;

    @OneToMany(mappedBy = "markedSubmission")
    private Set<MarkedAnswer> markedAnswers;

    // Constructors
    public MarkedSubmission() {

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

    // FilePath
    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
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
}
