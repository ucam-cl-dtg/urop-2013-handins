package uk.ac.cam.sup.structures;

import uk.ac.cam.sup.models.ProposedQuestion;

import java.util.LinkedList;
import java.util.List;

public class StudentSubmissions {

    public class AnsweredQuestion {
        // Fields
        private ProposedQuestion question;

        private String filePath;
        private String link;

        private boolean submitted;
        private boolean marked;

        // Constructors
        public AnsweredQuestion() {

        }

        public AnsweredQuestion(ProposedQuestion question, String filePath, String link, boolean submitted, boolean marked) {
            setQuestion(question);

            setFilePath(filePath);
            setLink(link);

            setSubmitted(submitted);
            setMarked(marked);
        }

        // FilePath
        public void setFilePath(String filePath) {
            this.filePath = filePath;
        }

        public String getFilePath() {
            return filePath;
        }

        // Marked
        public void setMarked(boolean marked) {
            this.marked = marked;
        }

        public boolean isMarked() {
            return marked;
        }

        // Submitted
        public void setSubmitted(boolean submitted) {
            this.submitted = submitted;
        }

        public boolean isSubmitted() {
            return submitted;
        }

        // Question
        public ProposedQuestion getQuestion() {
            return question;
        }

        public void setQuestion(ProposedQuestion question) {
            this.question = question;
        }

        // Link
        public String getLink() {
            return link;
        }

        public void setLink(String link) {
            this.link = link;
        }
    }

    private String name;

    private List<AnsweredQuestion> answeredQuestions = new LinkedList<AnsweredQuestion>();

    private boolean marked;

    // Constructors
    public StudentSubmissions() {

    }

    public StudentSubmissions(String name, boolean marked) {
        setName(name);
        setAnsweredQuestions(new LinkedList<AnsweredQuestion>());
    }

    // Name
    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    // Questions
    public void setAnsweredQuestions(List<AnsweredQuestion> answeredQuestions) {
        this.answeredQuestions = answeredQuestions;
    }

    public void addAnsweredQuestions(ProposedQuestion question, String filePath, String link,
                                     boolean submitted, boolean marked) {
        this.answeredQuestions.add(new AnsweredQuestion(question, filePath, link, submitted, marked));
    }

    public List<AnsweredQuestion> getAnsweredQuestions() {
        return answeredQuestions;
    }

    // Marked
    public boolean getMarked() {
        return marked;
    }

    public void setMarked(boolean marked) {
        this.marked = marked;
    }
}
