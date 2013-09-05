package uk.ac.cam.sup.models;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.persistence.*;

import com.google.common.collect.ImmutableMap;
import org.apache.commons.lang3.RandomStringUtils;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;

import uk.ac.cam.sup.HibernateUtil;
import uk.ac.cam.sup.helpers.UserHelper;

import com.sun.istack.NotNull;

@Entity
@Table(name = "Bin")
public class Bin {
    // Fields
    @Id
    @GeneratedValue(strategy=GenerationType.SEQUENCE, generator="binIdSeq")
    @SequenceGenerator(name="binIdSeq", sequenceName="BinSEQ", allocationSize=1)
    private long id;

    private Date dateCreated;

    private String owner;
    private String token;
    private String name;

    private boolean isArchived;
    private boolean peerMarking;

    @OneToMany(mappedBy = "bin")
    private Set<BinAccessPermission> accessPermissions;

    @OneToMany(mappedBy = "bin")
    private Set<BinDosAccess> dosAccess;

    @OneToMany(mappedBy = "bin")
    private Set<BinUserMarkingPermission> userMarkingPermissions;

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

    public Bin(String owner, String name) {
        dateCreated = new Date();

        setName(name);
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

    // Name
    public String getName() {
        if (name == null)
            return "";
        return name;
    }

    public void setName(String name) {
        this.name = name;
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

    // UserMarkingPermissions
    public Set<BinUserMarkingPermission> getUserMarkingPermissions() {
        return userMarkingPermissions;
    }

    public void setUserMarkingPermissions(Set<BinUserMarkingPermission> userMarkingPermissions) {
        this.userMarkingPermissions = userMarkingPermissions;
    }

    // Actual useful functions

    public static String generateToken() {
        return RandomStringUtils.randomAlphabetic(35);
    }

    public boolean isOwner(String user) {
        return user != null && user.equals(owner);
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

        if (user == null)
            return false;

        @SuppressWarnings("unchecked")
        List<BinAccessPermission> permissions = session.createCriteria(BinAccessPermission.class)
                                                       .add(Restrictions.eq("userCrsId", user))
                                                       .add(Restrictions.eq("bin", this)).list();

        return hasTotalAccess(user) || (!permissions.isEmpty());
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
        return hasTotalAccess(user) || isDos(user) || canAddSubmission(user);
    }

    /*
     * The Owner SHOULDN'T be able to delete the unmarkedSubmission The Dos
     * SHOULDN'T be able to see delete the unmarkedSubmission The Admin should
     * be able to delete the unmarkedSubmission The User who uploaded the
     * unmarkedSubmission should be able to delete it
     */
    public boolean canDeleteSubmission(String user, Submission<?> submission) {
        return hasTotalAccess(user, token) || submission.getOwner().equals(user);

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

        if (user == null)
            return false;

        @SuppressWarnings("unchecked")
        List<BinMarkingPermission> permissions = session.createCriteria(BinMarkingPermission.class)
                                                        .add(Restrictions.eq("userCrsId", user))
                                                        .add(Restrictions.eq("bin", this)).list();

        @SuppressWarnings("unchecked")
        List<BinUserMarkingPermission> userPermissions = session.createCriteria(BinUserMarkingPermission.class)
                                                                .add(Restrictions.eq("userCrsId", user))
                                                                .add(Restrictions.eq("bin", this))
                                                                .list();

        return hasTotalAccess(user) || isPeerMarking() || (!permissions.isEmpty()) || (!userPermissions.isEmpty());
    }

    public boolean canSeeAnswer(String user, Answer answer) {

        Session session = HibernateUtil.getSession();

        //TODO will this crash if user is null ?!
        @SuppressWarnings("unchecked")
        List<BinMarkingPermission> permissions = session.createCriteria(BinMarkingPermission.class)
                                                        .add(Restrictions.eq("userCrsId", user))
                                                        .add(Restrictions.eq("bin", this))
                                                        .add(Restrictions.eq("questionId", answer.getQuestion().getId()))
                                                        .add(Restrictions.eq("questionOwner", answer.getOwner()))
                                                        .list();

        @SuppressWarnings("unchecked")
        List<BinUserMarkingPermission> userPermissions = session.createCriteria(BinUserMarkingPermission.class)
                                                                .add(Restrictions.eq("userCrsId", user))
                                                                .add(Restrictions.eq("bin", this))
                                                                .list();

        return hasTotalAccess(user) || isPeerMarking() || answer.getOwner().equals(user) || (!permissions.isEmpty()) || (!userPermissions.isEmpty());
    }

    public boolean canSeeAnnotated(String user, MarkedAnswer answer) {
        return hasTotalAccess(user) || isPeerMarking() || isDos(user) || answer.getAnnotator().equals(user) || answer.getOwner().equals(user);
    }

    @Transient
    public int getQuestionCount() {
        return questionSet.size();
    }

    @Transient
    public boolean isDos(String user) {

        Session session = HibernateUtil.getSession();

        @SuppressWarnings("unchecked")
        List<BinDosAccess> Doses = session.createCriteria(BinDosAccess.class)
                                          .add(Restrictions.eq("userCrsId", user))
                                          .add(Restrictions.eq("bin", this))
                                          .list();

        return !Doses.isEmpty();
    }

    public static Object toJSON(List<Bin> bins) {
        List result = new LinkedList();

        for (Bin task: bins)
            //noinspection unchecked
            result.add(task.toJSON());

        return result;
    }

    public Object toJSON() {
        ImmutableMap.Builder<String, Object> builder = new ImmutableMap.Builder<String, Object>();

        return builder.put("id", getId())
                      .put("name", getName())
                      .put("owner", getOwner())
                      .put("dateCreated", getDateCreated())
                      .put("archived", isArchived())
                      .put("peerMarking", isPeerMarking())
                      .build();
    }
}