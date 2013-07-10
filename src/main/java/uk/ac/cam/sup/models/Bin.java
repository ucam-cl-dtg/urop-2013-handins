package uk.ac.cam.sup.models;

import org.hibernate.Session;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import uk.ac.cam.sup.HibernateUtil;

import javax.persistence.*;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "Bin")
public class Bin {
    // Fields
    private long id;
    private Set<BinPermission> permissions;
    private String question;
    private String owner;

    public Bin() {}

    //Getters
    @Id
    @GeneratedValue(generator="increment")
    @GenericGenerator(name="increment", strategy="increment")
    public long getId() { return id; }

    @OneToMany(mappedBy="bin")
    public Set<BinPermission> getPermissions(){ return permissions; }

    public String getQuestion() { return question; }
    public String getOwner() { return owner; }


    //Setters
    public void setId(long id) { this.id = id; }
    public void setPermissions(Set<BinPermission> permissions) {
        this.permissions = permissions;
    }
    public void setQuestion(String question) { this.question = question; }
    public void setOwner(String user) { this.question = question; }


    // Actual useful functions
    public boolean isDos(String user) {
        return false;
    }

    public boolean isAdmin(String user) {
        return false;
    }

    public boolean isOwner(String user) {
        return user == owner;
    }
    /*
    A dos can see anything
    An admin can see anything
    The supervisor can see anything
    */

    public boolean canSeeAll(String user) {
        return isDos(user) || isAdmin(user) || isOwner(user);
    }

    /*
    Only users with permissions can upload files.
    TODO  Should the owner be able to upload ?!. (No atm)
    */

    public boolean canAddSubmission(String user) {
        Session session = HibernateUtil.getSF().getCurrentSession();
        Integer permission = (Integer) session.createCriteria(BinPermission.class)
               .add(Restrictions.eq("user", user))
               .setProjection(Projections.rowCount())
               .list().get(0);

        return (permission != 0);
    }

    /*
    The Owner should be able to see any submissions
    The Dos should be able to see any submissions
    The Admin should be able to see any submissions
    The User who uploaded the submission should be able to see it
     */

    public boolean canSeeSubmission(String user, Submission submission) {
        if (isOwner(user) || isAdmin(user) || isDos(user)) {
            return true;
        }
        return submission.getUser() == user;
    }

    /*
    The Owner SHOULDN'T be able to delete the submission
    The Dos SHOULDN'T be able to see delete the submission
    The Admin should be able to delete the submission
    The User who uploaded the submission should be able to delete it
     */
    public boolean canDeleteSubmission(String user, Submission submission) {
        if (isAdmin(user)) {
            return true;
        }

        return submission.getUser() == user;
    }


}
