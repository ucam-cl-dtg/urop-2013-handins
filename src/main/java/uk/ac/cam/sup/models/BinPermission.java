package uk.ac.cam.sup.models;

import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name = "BinPermission")
public class BinPermission {
    // Fields
    @Id
    @GeneratedValue(generator="increment")
    @GenericGenerator(name="increment", strategy="increment")
    private long id;

    private Date dateCreated;

    @Column(name="`user`")
    private String user;

    @ManyToOne
    private Bin bin;

    // Constructors
    public BinPermission() {
        dateCreated = new Date();
    }

    public BinPermission(Bin bin, String user) {
        dateCreated = new Date();

        setBin(bin);
        setUser(user);
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

    // User
    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    // DateCreated
    public void setDateCreated(Date dateCreated) {
        this.dateCreated = dateCreated;
    }

    public Date getDateCreated() {
        return dateCreated;
    }
}
