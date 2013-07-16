package uk.ac.cam.sup.structures;

public class AnsweredQuestion {
    // Fields
    private String name;
    private String downloadPath;
    private boolean submitted;
    private boolean marked;

    // Constructors
    public AnsweredQuestion() {

    }

    public AnsweredQuestion(String name, String downloadPath, boolean submitted, boolean marked) {
        setName(name);
        setDownloadPath(downloadPath);
        setSubmitted(submitted);
        setMarked(marked);
    }

    // Name
    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    // DownloadPath
    public void setDownloadPath(String downloadPath) {
        this.downloadPath = downloadPath;
    }

    public String getDownloadPath() {
        return downloadPath;
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
}
