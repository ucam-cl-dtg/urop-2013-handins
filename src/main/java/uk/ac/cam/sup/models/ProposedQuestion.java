package uk.ac.cam.sup.models;

// TODO: KILL ME!

import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.util.Set;

@Entity
@Table(name = "ProposedQuestion")
public class ProposedQuestion {
    // Fields
    @Id
    @GeneratedValue(generator="increment")
    @GenericGenerator(name="increment", strategy="increment")
    private long id;

    private String name;

    @ManyToOne
    private Bin bin;

    @OneToMany(mappedBy = "question")
    private Set<Answer> answers;

    // Constructors
    public ProposedQuestion() {

    }

    // Id
    public long getId() {
        return id;
    }

    // Name
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    // Bin
    public Bin getBin() {
        return bin;
    }

    public void setBin(Bin bin) {
        this.bin = bin;
    }

    // Answers
    public Set<Answer> getAnswers() {
        return answers;
    }

    public void setAnswers(Set<Answer> answers) {
        this.answers = answers;
    }
}
