package uk.ac.cam.sup.structures;

import uk.ac.cam.sup.models.ProposedQuestion;

public class Marking {
    // Fields

    private String filePath;

    private int first = 0;
    private int last = 0;

    private String owner;
    private ProposedQuestion question;

    // Constructors

    public Marking() {

    }

    public Marking(String filePath) {
        setFilePath(filePath);
    }

    // FilePath
    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    // First
    public int getFirst() {
        return first;
    }

    public void setFirst(int first) {
        this.first = first;
    }

    // Last
    public int getLast() {
        return last;
    }

    public void setLast(int last) {
        this.last = last;
    }

    // Owner
    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    // QuestionId
    public ProposedQuestion getQuestion() {
        return question;
    }

    public void setQuestion(ProposedQuestion question) {
        this.question = question;
    }
}
