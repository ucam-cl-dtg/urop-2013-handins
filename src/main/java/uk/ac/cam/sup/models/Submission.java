package uk.ac.cam.sup.models;

import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.util.Set;

@Entity
@Table(name = "Submission")
public class Submission {
    // Fields
    private long id;
    private String filepath;
    private String user;
    //private Set<Answer> answers;


    //Getters
    @Id
    @GeneratedValue(generator="increment")
    @GenericGenerator(name="increment", strategy="increment")
    public long getId() { return id; }

    /*@OneToMany(mappedBy="bin")
    public Set<BinPermission> getPermissions(){ return permissions; } */

    public String getQFilepath() { return filepath; }
    public String getUser() { return user; }


    //Setters
    public void setId(long id) { this.id = id; }
    /*public void setPermissions(Set<BinPermission> permissions) {
        this.permissions = permissions;
    } */
    public void setFilepath(String filepath) { this.filepath = filepath; }
    public void setUser(String user) { this.user = user; }



}