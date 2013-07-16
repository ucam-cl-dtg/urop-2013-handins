package uk.ac.cam.sup.structures;

import java.util.LinkedList;
import java.util.List;

public class Student {
    private String name;
    private boolean marked;
    private List<Question> questions;

    // Constructors
    public Student() {

    }

    public Student(String name, boolean marked) {
        setName(name);
        setMarked(marked);
        setQuestions(new LinkedList<Question>());
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
    public void setQuestions(List questions) {
        this.questions = questions;
    }

    public void addQuestion(Question question) {
        this.questions.add(question);
    }

    public List<Question> getQuestions() {
        return questions;
    }
}
