package uk.ac.cam.sup.models;

import com.sun.istack.NotNull;
import org.apache.commons.lang3.RandomStringUtils;
import org.hibernate.Session;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import uk.ac.cam.sup.HibernateUtil;
import uk.ac.cam.sup.helpers.UserHelper;

import javax.persistence.*;
import java.util.Set;

@Entity
@Table(name = "Bin")
public class Bin {
    // Fields
    private long id;
    private Set<BinPermission> permissions;
    private String questionSet;
    private String owner;
    private String token;

    public Bin() {}
    public Bin(String owner, String questionSet) {
        setQuestionSet(questionSet);
        setOwner(owner);
        setToken(generateToken());
    }

    //Getters
    @Id
    @GeneratedValue(generator="increment")
    @GenericGenerator(name="increment", strategy="increment")
    public long getId() { return id; }

    @OneToMany(mappedBy="bin")
    public Set<BinPermission> getPermissions(){ return permissions; }

    public String getQuestionSet() {
        if (questionSet == null)
            return "";
        return questionSet;
    }

    @NotNull
    public String getOwner() { return owner; }
    public String getToken() { return token; }


    //Setters
    public void setId(long id) { this.id = id; }
    public void setPermissions(Set<BinPermission> permissions) {
        this.permissions = permissions;
    }
    public void setQuestionSet(String questionSet) { this.questionSet = questionSet; }
    public void setOwner(String owner) { this.owner = owner; }
    public void setToken(String token) { this.token = token; }


    // Actual useful functions

    public static String generateToken() {
        String res = RandomStringUtils.randomAlphabetic(35);
        return res;
    }

    public boolean isOwner(String user) {
        return user.equals(owner);
    }

    public boolean canDelete(String token) {
        return token.equals(this.token);
    }
    /*
    A dos can see anything
    An admin can see anything
    The supervisor can see anything
    */

    public boolean canSeeAll(String user) {
        return UserHelper.isDos(user) || UserHelper.isAdmin(user) || isOwner(user);
    }

    /*
    Only users with permissions can upload files.
    TODO  Should the owner be able to upload ?!. (No atm)
    */

    public boolean canAddSubmission(String user) {
        Session session = HibernateUtil.getSession();
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
        if (isOwner(user) || UserHelper.isAdmin(user) || UserHelper.isDos(user)) {
            return true;
        }
        return submission.getUser().equals(user);
    }

    /*
    The Owner SHOULDN'T be able to delete the submission
    The Dos SHOULDN'T be able to see delete the submission
    The Admin should be able to delete the submission
    The User who uploaded the submission should be able to delete it
     */
    public boolean canDeleteSubmission(String user, Submission submission) {
        if (UserHelper.isAdmin(user)) {
            return true;
        }

        return submission.getUser().equals(user);
    }

    /*
     Only by using a valid token can people modify the bin permissions
     */

    public boolean canAddPermission(String token) {
        return this.token.equals(token);
    }

    public boolean canDeletePermission(String token) {
        return this.token.equals(token);
    }


}
