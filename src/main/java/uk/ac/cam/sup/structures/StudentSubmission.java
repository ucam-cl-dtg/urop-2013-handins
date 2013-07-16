package uk.ac.cam.sup.structures;

import java.util.LinkedList;
import java.util.List;

public class StudentSubmission {
    private String name;
    private boolean marked;
    private List<AnsweredQuestion> answeredQuestions;

    // Constructors
    public StudentSubmission() {

    }

    public StudentSubmission(String name, boolean marked) {
        setName(name);
        setMarked(marked);
        setAnsweredQuestions(new LinkedList<AnsweredQuestion>());
    }

    // Name
    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    // Marked
    public void setMarked(boolean marked) {
        this.marked = marked;
    }

    public boolean getMarked() {
        return marked;
    }

    // Questions
    public void setAnsweredQuestions(List answeredQuestions) {
        this.answeredQuestions = answeredQuestions;
    }

    public void addAnsweredQuestion(AnsweredQuestion answeredQuestion) {
        this.answeredQuestions.add(answeredQuestion);
    }

    public List<AnsweredQuestion> getAnsweredQuestions() {
        return answeredQuestions;
    }
}
