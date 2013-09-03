package uk.ac.cam.sup.models;

import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name = "BinMarkingPermission")
public class BinMarkingPermission {
    // Fields
    @Id
    @GeneratedValue(strategy=GenerationType.SEQUENCE, generator="binMarkingPermissionIdSeq")
    @SequenceGenerator(name="binMarkingPermissionIdSeq", sequenceName="binMarkingPermissionSEQ", allocationSize=1)
    private long id;

    private Date dateCreated;

    private String userCrsId;
    private long questionId;
    private String questionOwner;

    @ManyToOne
    private Bin bin;

    // Constructors
    public BinMarkingPermission() {
        dateCreated = new Date();
    }

    public BinMarkingPermission(Bin bin, String userCrsId, long questionId, String questionOwner) {
        dateCreated = new Date();

        setBin(bin);

        setUserCrsId(userCrsId);
        setQuestionId(questionId);
        setQuestionOwner(questionOwner);
    }

    // Id
    public long getId() {
        return id;
    }

    // Bin
    public Bin getBin() {
        return bin;
    }

    public void setBin(Bin bin) {
        this.bin = bin;
    }

    // UserCrsId
    public String getUserCrsId() {
        return userCrsId;
    }

    public void setUserCrsId(String userCrsId) {
        this.userCrsId = userCrsId;
    }

    // DateCreated
    public void setDateCreated(Date dateCreated) {
        this.dateCreated = dateCreated;
    }

    public Date getDateCreated() {
        return dateCreated;
    }

    // QuestionOwner
    public String getQuestionOwner() {
        return questionOwner;
    }

    public void setQuestionOwner(String questionOwner) {
        this.questionOwner = questionOwner;
    }

    // Question
    public long getQuestionId() {
        return questionId;
    }

    public void setQuestionId(long questionId) {
        this.questionId = questionId;
    }
}
