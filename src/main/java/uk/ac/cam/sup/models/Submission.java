package uk.ac.cam.sup.models;

import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.util.Set;

@MappedSuperclass
public abstract class Submission<T> {
    // Fields
    @Id
    @GeneratedValue(generator="increment")
    @GenericGenerator(name="increment", strategy="increment")
    private long id;

    private String owner;
    private String filePath;

    @ManyToOne
    private Bin bin;

    // Constructors
    public Submission() {

    }

    public Submission(String owner) {
        setOwner(owner);
    }

    // Id
    public long getId() {
        return id;
    }

    // Answers
    public abstract Set<T> getAllAnswers();
    public abstract void setAllAnswers(Set<T> answers);

    // Bin
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

    // Owner
    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }
}
