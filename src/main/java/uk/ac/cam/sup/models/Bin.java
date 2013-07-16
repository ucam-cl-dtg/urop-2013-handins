package uk.ac.cam.sup.models;

import com.sun.istack.NotNull;
import org.apache.commons.lang3.RandomStringUtils;
import org.hibernate.Session;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import uk.ac.cam.sup.HibernateUtil;
import uk.ac.cam.sup.helpers.UserHelper;
import uk.ac.cam.sup.structures.ProposedQuestion;

import javax.persistence.*;
import java.util.Set;

@Entity
@Table(name = "Bin")
public class Bin {
    // Fields
    private long id;

    private String owner;
    private String token;
    private String questionSetName;

    private Set<BinPermission> permissions;
    private Set<Submission> submissions;
    private Set<Answer> answers;
    private Set<MarkedSubmission> markedSubmissions;
    private Set<ProposedQuestion> questionSet;

    // Constructors
    public Bin() {

    }

    public Bin(String owner, String questionSetName) {
        setQuestionSetName(questionSetName);
        setOwner(owner);
        setToken(generateToken());
    }

    // Id
    @Id
    @GeneratedValue(generator="increment")
    @GenericGenerator(name="increment", strategy="increment")
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    // Permissions
    @OneToMany(mappedBy="bin")
    public Set<BinPermission> getPermissions(){
        return permissions;
    }

    public void setPermissions(Set permissions) {
        this.permissions = permissions;
    }

    // Submissions
    @OneToMany(mappedBy="bin")
    public Set<Submission> getSubmissions(){
        return submissions;
    }

    public void setSubmissions(Set submissions) {
        this.submissions = submissions;
    }

    // QuestionSetName
    public String getQuestionSetName() {
        if (questionSetName == null)
            return "";
        return questionSetName;
    }

    public void setQuestionSetName(String questionSetName) {
        this.questionSetName = questionSetName;
    }

    // Owner
    @NotNull
    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    // Token
    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    // Answers
    @OneToMany(mappedBy = "bin")
    public Set<Answer> getAnswers() {
        return answers;
    }

    public void setAnswers(Set answers) {
        this.answers = answers;
    }

    // MarkedSubmissions
    @OneToMany(mappedBy = "bin")
    public Set<MarkedSubmission> getMarkedSubmissions() {
        return markedSubmissions;
    }

    public void setMarkedSubmissions(Set markedSubmissions) {
        this.markedSubmissions = markedSubmissions;
    }

    // Actual useful functions
    public static String generateToken() {
        return RandomStringUtils.randomAlphabetic(35);
    }

    public boolean isOwner(String user) {
        return user.equals(owner);
    }

    public boolean canDelete(String token) {

        // return token.equals(this.token);
        // FIXME Is there any reason to allow deletion of bins?
        return false;
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
        Long permission = (Long) session.createCriteria(BinPermission.class)
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

        return submission.getOwner().equals(user);
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

        return submission.getOwner().equals(user);
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

    /*
    ToDo: complete the function and add the comments
     */
    public boolean canAddMarkedSubmission(String user) {
        return true;
    }

    /*
    ToDo: complete the function and add the comments
     */
    public boolean canSeeAnswer(String user, Answer answer) {
        return true;
    }

    @Transient
    public int getQuestionCount() {
        return Integer.parseInt(RandomStringUtils.randomAlphabetic(1));
    }
}