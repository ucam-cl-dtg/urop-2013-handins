package uk.ac.cam.sup.structures;

public class Distribution {
    // Fields
    private int startPage;
    private int endPage;
    private String student;
    private String question;
    private long submissionId;

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
    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    // SubmissionId
    public long getSubmissionId() {
        return submissionId;
    }

    public void setSubmissionId(long submissionId) {
        this.submissionId = submissionId;
    }
}
