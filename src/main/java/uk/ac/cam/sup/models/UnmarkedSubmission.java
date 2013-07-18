package uk.ac.cam.sup.models;

import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.util.Set;

@Entity
@Table(name = "Submission")

@MappedSuperclass
public abstract class UnmarkedSubmission {
    // Fields
    @Id
    @GeneratedValue(generator="increment")
    @GenericGenerator(name="increment", strategy="increment")
    private long id;

    private String owner;
    private String filePath;

    @ManyToOne
    private Bin bin;

    @OneToMany(mappedBy="submission")
    private Set<Answer> answers;

    // Constructors
    public UnmarkedSubmission() {

    }

    public UnmarkedSubmission(String owner) {
        setOwner(owner);
    }

    // Id
    public long getId() {
        return id;
    }

    // Answers
    public Set<Answer> getAnswers() {
        return answers;
    }

    public void setAnswers(Set answers) {
        this.answers = answers;
    }

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

    // TODO make this work when the site is mounted at a random point
    @Transient
    public String getLink() {
        return "/submission/" + getBin().getId() + "/" + getId();
    }
}
