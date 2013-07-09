package uk.ac.cam.sup.models;

import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.util.Set;

@Entity
@Table(name = "Bin")
public class Bin {
    // Fields
    private long id;
    private Set<BinPermission> permissions;
    private String question;


    //Getters
    @Id
    @GeneratedValue(generator="increment")
    @GenericGenerator(name="increment", strategy="increment")
    public long getId() { return id; }

    @OneToMany(mappedBy="bin")
    public Set<BinPermission> getPermissions(){ return permissions; }

    public String getQuestion() { return question; }


    //Setters
    public void setId(long id) { this.id = id; }
    public void setPermissions(Set<BinPermission> permissions) {
        this.permissions = permissions;
    }
    public void setQuestion(String question) { this.question = question; }



}
