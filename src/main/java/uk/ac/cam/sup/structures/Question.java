package uk.ac.cam.sup.structures;

public class Question {
    private String name;
    private String downloadPath;
    private boolean marked;

    // Constructors
    public Question() {

    }

    public Question(String name, String downloadPath, boolean marked) {
        this.name = name;
        this.downloadPath = downloadPath;
        this.marked = marked;
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

    public boolean getMarked() {
        return marked;
    }
}
