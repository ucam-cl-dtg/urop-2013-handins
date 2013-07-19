package uk.ac.cam.sup.structures;

import uk.ac.cam.sup.models.ProposedQuestion;

import java.util.LinkedList;
import java.util.List;

public class QuestionSubmissions {
    public class StudentPart {
        // Fields

        private String filePath;
        private String link;

        private boolean submitted;
        private boolean marked;

        // Constructors
        public StudentPart() {

        }

        public StudentPart(String filePath, String link, boolean submitted, boolean marked) {
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

        // Link
        public String getLink() {
            return link;
        }

        public void setLink(String link) {
            this.link = link;
        }
    }

    private ProposedQuestion question;

    private String name;

    private List<StudentPart> students = new LinkedList<StudentPart>();

    private boolean marked;

    // Constructors
    public QuestionSubmissions() {

    }

    public QuestionSubmissions(String name, boolean marked) {
        setName(name);
        setStudents(new LinkedList<StudentPart>());
    }

    // Name
    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    // Questions
    public void setStudents(List<StudentPart> students) {
        this.students = students;
    }

    public void addStudent(String filePath, String link,
                                     boolean submitted, boolean marked) {
        students.add(new StudentPart(filePath, link, submitted, marked));
    }

    public List<StudentPart> getStudents() {
        return students;
    }

    // Marked
    public boolean getMarked() {
        return marked;
    }

    public void setMarked(boolean marked) {
        this.marked = marked;
    }

    // Question
    public ProposedQuestion getQuestion() {
        return question;
    }

    public void setQuestion(ProposedQuestion question) {
        this.question = question;
    }
}

