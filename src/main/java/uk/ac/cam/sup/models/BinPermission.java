package uk.ac.cam.sup.models;

import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;

@Entity
@Table(name = "BinPermission")
public class BinPermission {
    // Fields
    private long id;

    private String user;

    private Bin bin;

    // Constructors
    public BinPermission() {

    }

    public BinPermission(Bin bin, String user) {
        setBin(bin);
        setUser(user);
    }

    //Getters
    @Id
    @GeneratedValue(generator="increment")
    @GenericGenerator(name="increment", strategy="increment")
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    // Bin
    @ManyToOne
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
}
