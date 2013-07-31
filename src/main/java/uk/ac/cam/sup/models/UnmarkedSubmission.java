package uk.ac.cam.sup.models;

import javax.persistence.*;
import java.util.Set;

@Entity
@Table(name = "UnmarkedSubmission")
public class UnmarkedSubmission extends Submission<Answer> {
    // Fields
    @OneToMany(mappedBy="unmarkedSubmission")
    private Set<Answer> answers;

    // Constructors
    public UnmarkedSubmission() {
        super();
    }

    public UnmarkedSubmission(String owner) {
        super(owner);
    }

    // Answers
    public Set<Answer> getAllAnswers() {
        return answers;
    }

    public void setAllAnswers(Set<Answer> answers) {
        this.answers = answers;
    }

    // Actual useful functions

    public String getFolder() {
        return "answer";
    }
}
