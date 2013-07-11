package uk.ac.cam.sup.models;

import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;

@Entity
@Table(name = "BinPermission")
public class BinPermission {
    // Fields
    private long id;
    private Bin bin;
    private String user;

    public BinPermission() {}
    public BinPermission(Bin bin, String user) {
        setBin(bin);
        setUser(user);
    }

    //Getters
    @Id
    @GeneratedValue(generator="increment")
    @GenericGenerator(name="increment", strategy="increment")
    public long getId() { return id; }

    @ManyToOne
    public Bin getBin() { return bin; }
    public String getUser() { return user; }

    //Setters
    public void setId(long id) { this.id = id; }
    public void setBin(Bin bin) { this.bin = bin; }
    public void setUser(String user) { this.user = user; }

}
