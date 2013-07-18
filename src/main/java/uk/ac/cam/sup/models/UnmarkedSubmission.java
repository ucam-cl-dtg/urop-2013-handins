package uk.ac.cam.sup.models;

import org.hibernate.annotations.GenericGenerator;

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

    // TODO make this work when the site is mounted at a random point
    @Transient
    public String getLink() {
        return "/submission/" + getId();
    }
}
