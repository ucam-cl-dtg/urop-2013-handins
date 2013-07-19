package uk.ac.cam.sup.structures;

import uk.ac.cam.sup.models.ProposedQuestion;
import uk.ac.cam.sup.models.Submission;

public class Distribution {
    // Fields
    private int startPage;
    private int endPage;

    private String student;

    private ProposedQuestion question;

    private Submission submission;

    // Constructors
    public Distribution() {

    }

    // StartPage
    public int getStartPage() {
        return startPage;
    }

    public void setStartPage(int startPage) {
        this.startPage = startPage;
    }

    // EndPage
    public int getEndPage() {
        return endPage;
    }

    public void setEndPage(int endPage) {
        this.endPage = endPage;
    }

    // Student
    public String getStudent() {
        return student;
    }

    public void setStudent(String student) {
        this.student = student;
    }

    // Question
    public ProposedQuestion getQuestion() {
        return question;
    }

    public void setQuestion(ProposedQuestion question) {
        this.question = question;
    }

    // Submission
    public Submission getSubmission() {
        return submission;
    }

    public void setSubmission(Submission submission) {
        this.submission = submission;
    }
}
