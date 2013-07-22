package uk.ac.cam.sup.models;

import javax.persistence.*;
import java.util.Set;

@Entity
@Table(name = "MarkedSubmission")
public class MarkedSubmission extends Submission<MarkedAnswer> {
    // Fields
    @OneToMany(mappedBy="markedSubmission")
    private Set<MarkedAnswer> markedAnswers;

    // Constructors
    public MarkedSubmission() {

    }

    public MarkedSubmission(String owner) {
        super(owner);
    }

    // MarkedAnswers
    public Set<MarkedAnswer> getAllAnswers() {
        return markedAnswers;
    }

    public void setAllAnswers(Set<MarkedAnswer> markedAnswers) {
        this.markedAnswers = markedAnswers;
    }

    // Actual useful functions

    public String getFolder() {
        return "annotated";
    }
}
