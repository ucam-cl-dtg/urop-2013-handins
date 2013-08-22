package uk.ac.cam.sup.models;

import java.util.Date;
import java.util.List;
import java.util.Set;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.apache.commons.lang3.RandomStringUtils;
import org.hibernate.Session;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.criterion.Restrictions;

import uk.ac.cam.sup.HibernateUtil;
import uk.ac.cam.sup.helpers.UserHelper;

import com.sun.istack.NotNull;

@Entity
@Table(name = "Bin")
public class Bin {
    // Fields
    @Id
    @GeneratedValue(generator = "increment")
    @GenericGenerator(name = "increment", strategy = "increment")
    private long id;

    private Date dateCreated;

    private String owner;
    private String token;
    private String questionSetName;

    private boolean isArchived;
    private boolean peerMarking;

    @OneToMany(mappedBy = "bin")
    private Set<BinAccessPermission> accessPermissions;

    @OneToMany(mappedBy = "bin")
    private Set<BinMarkingPermission> markingPermissions;

    @OneToMany(mappedBy = "bin")
    private Set<UnmarkedSubmission> unmarkedSubmissions;

    @OneToMany(mappedBy = "bin")
    private Set<Answer> answers;

    @OneToMany(mappedBy = "bin")
    private Set<MarkedSubmission> markedSubmissions;

    @OneToMany(mappedBy = "bin")
    private Set<ProposedQuestion> questionSet;

    // Constructors
    public Bin() {
        dateCreated = new Date();
    }

    public Bin(String owner, String questionSetName) {
        dateCreated = new Date();

        setQuestionSetName(questionSetName);
        setOwner(owner);
        setToken(generateToken());
    }

    // Id
    public long getId() {
        return id;
    }

    // AccessPermissions
    public Set<BinAccessPermission> getAccessPermissions() {
        return accessPermissions;
    }

    public void setPermissions(Set<BinAccessPermission> accessPermissions) {
        this.accessPermissions = accessPermissions;
    }

    // Submissions
    public Set<UnmarkedSubmission> getUnmarkedSubmissions() {
        return unmarkedSubmissions;
    }

    public void setUnmarkedSubmissions(
            Set<UnmarkedSubmission> unmarkedSubmissions) {
        this.unmarkedSubmissions = unmarkedSubmissions;
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
    public Set<Answer> getAnswers() {
        return answers;
    }

    public void setAnswers(Set<Answer> answers) {
        this.answers = answers;
    }

    @Transient
    public String getName() {
        return getQuestionSetName();
    }

    // MarkedSubmissions
    public Set<MarkedSubmission> getMarkedSubmissions() {
        return markedSubmissions;
    }

    public void setMarkedSubmissions(Set<MarkedSubmission> markedSubmissions) {
        this.markedSubmissions = markedSubmissions;
    }

    // QuestionSet
    public Set<ProposedQuestion> getQuestionSet() {
        return questionSet;
    }

    public void addQuestion(ProposedQuestion question) {
        questionSet.add(question);
    }

    public void setQuestionSet(Set<ProposedQuestion> questionSet) {
        this.questionSet = questionSet;
    }

    // Archived
    public boolean isArchived() {
        return isArchived;
    }

    public void setArchived(boolean archived) {
        isArchived = archived;
    }

    // DateCreated
    public void setDateCreated(Date dateCreated) {
        this.dateCreated = dateCreated;
    }

    public Date getDateCreated() {
        return dateCreated;
    }

    // MarkingPermissions
    public Set<BinMarkingPermission> getMarkingPermissions() {
        return markingPermissions;
    }

    public void setMarkingPermissions(
            Set<BinMarkingPermission> markingPermissions) {
        this.markingPermissions = markingPermissions;
    }

    // PeerMarking
    public boolean isPeerMarking() {
        return peerMarking;
    }

    public void setPeerMarking(boolean peerMarking) {
        this.peerMarking = peerMarking;
    }

    // Actual useful functions

    public static String generateToken() {
        return RandomStringUtils.randomAlphabetic(35);
    }

    public boolean isOwner(String user) {
        return user.equals(owner);
    }

    public boolean hasTotalAccess(String user) {
        return hasTotalAccess(user, null);
    }

    public boolean hasTotalAccess(String user, String token) {
        return isOwner(user) || UserHelper.isAdmin(user) || this.token.equals(token);
    }

    public boolean canDelete(String user, String token) {
        return hasTotalAccess(user, token);
    }

    /*
     * Only users with permissions can upload files.
     */
    public boolean canAddSubmission(String user) {
        Session session = HibernateUtil.getSession();

        @SuppressWarnings("unchecked")
        List<BinAccessPermission> permissions = session.createCriteria(BinAccessPermission.class)
                                                       .add(Restrictions.eq("userCrsId", user))
                                                       .add(Restrictions.eq("bin", this)).list();

        return (!permissions.isEmpty());
    }

    /*
     * The Owner should be able to see any unmarkedSubmissions The Dos should be
     * able to see any unmarkedSubmissions The Admin should be able to see any
     * unmarkedSubmissions The User who uploaded the unmarkedSubmission should
     * be able to see it
     */
    public boolean canSeeSubmission(String user, UnmarkedSubmission unmarkedSubmission) {
        return hasTotalAccess(user) || unmarkedSubmission.getOwner().equals(user);

    }

    public boolean canUploadIntoBin(String user) {
        return hasTotalAccess(user) || UserHelper.isDos(user) || canAddSubmission(user);
    }

    /*
     * The Owner SHOULDN'T be able to delete the unmarkedSubmission The Dos
     * SHOULDN'T be able to see delete the unmarkedSubmission The Admin should
     * be able to delete the unmarkedSubmission The User who uploaded the
     * unmarkedSubmission should be able to delete it
     */
    public boolean canDeleteSubmission(String user, Submission<?> submission) {
        return isOwner(user) || UserHelper.isAdmin(user) || this.token.equals(token) || submission.getOwner().equals(user);

    }

    /*
     * Only by using a valid token can people modify the bin permissions
     */
    public boolean canAddPermission(String user, String token) {
        return hasTotalAccess(user, token);
    }

    public boolean canDeletePermission(String user, String token) {
        return hasTotalAccess(user, token);
    }

    public boolean canAddMarkedSubmission(String user) {
        Session session = HibernateUtil.getSession();

        @SuppressWarnings("unchecked")
        List<BinMarkingPermission> permissions = session.createCriteria(BinMarkingPermission.class)
                                                        .add(Restrictions.eq("userCrsId", user))
                                                        .add(Restrictions.eq("bin", this)).list();

        return isPeerMarking() || isOwner(user) || (!permissions.isEmpty());
    }

    public boolean canSeeAnswer(String user, Answer answer) {
        Session session = HibernateUtil.getSession();

        @SuppressWarnings("unchecked")
        List<BinMarkingPermission> permissions = session.createCriteria(BinMarkingPermission.class)
                                                        .add(Restrictions.eq("userCrsId", user))
                                                        .add(Restrictions.eq("bin", this))
                                                        .add(Restrictions.eq("questionId", answer.getQuestion().getId()))
                                                        .add(Restrictions.eq("questionOwner", answer.getOwner()))
                                                        .list();

        return isPeerMarking() || user.equals(answer.getOwner()) || (!permissions.isEmpty());
    }

    public boolean canSeeAnnotated(String user, MarkedAnswer answer) {
        return isPeerMarking() || hasTotalAccess(user) || UserHelper.isDos(user) || user.equals(answer.getAnnotator()) || user.equals(answer.getOwner());
    }

    @Transient
    public int getQuestionCount() {
        return questionSet.size();
    }
}