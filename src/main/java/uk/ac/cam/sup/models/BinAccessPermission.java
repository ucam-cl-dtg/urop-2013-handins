package uk.ac.cam.sup.models;

import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name = "BinAccessPermission")
public class BinAccessPermission {
    // Fields
    @Id
    @GeneratedValue(strategy=GenerationType.SEQUENCE, generator="binAccessPermissionIdSeq")
    @SequenceGenerator(name="binAccessPermissionIdSeq", sequenceName="BinAccessPermissionSEQ", allocationSize=1)
    private long id;

    private Date dateCreated;

    private String userCrsId;

    @ManyToOne
    private Bin bin;

    // Constructors
    public BinAccessPermission() {
        dateCreated = new Date();
    }

    public BinAccessPermission(Bin bin, String userCrsId) {
        dateCreated = new Date();

        setBin(bin);
        setUserCrsId(userCrsId);
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
}
