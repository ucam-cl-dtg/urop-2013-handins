package uk.ac.cam.sup.models;

import org.hibernate.annotations.GenericGenerator;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "MarkedSubmission")
public class MarkedSubmission {
    // Fields
    private String PDFpath;
    private int id;

    // Class
    MarkedSubmission() {

    }

    MarkedSubmission(String path) {
        PDFpath = path;
    }

    // Id
    @Id
    @GeneratedValue(generator="increment")
    @GenericGenerator(name="increment", strategy="increment")
    public int getId() {
        return id;
    }

    public void setId(int i) {
        id = i;
    }

    // FilePath
    public String getFilePath() {
        return PDFpath;
    }

    public void setFilePath(String path) {
        PDFpath = path;
    }
}
