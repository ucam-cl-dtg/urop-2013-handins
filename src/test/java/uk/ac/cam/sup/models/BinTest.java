package uk.ac.cam.sup.models;

import org.hibernate.Session;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import uk.ac.cam.sup.HibernateUtil;

//TODO add test for admins and dos
public class BinTest {

    final String testOwner = "at666";
    final String testQuestionSet = "/questionSet/666013";
    final String randomUser ="at667";
    final String perm1u = "fp628";
    final String perm2u = "sg235";

    Bin testBin;
    BinPermission perm1, perm2;
    Session session;
    Submission submission;

    @Before
    public void setUp() throws Exception {
        submission = new Submission(setUp);

        testBin = new Bin(testOwner,testQuestionSet);
        session = HibernateUtil.getSession();
        session.beginTransaction();
        session.save(testBin);

        session.save(perm1 = new BinPermission(testBin, perm1u));
        session.save(perm2 = new BinPermission(testBin, perm2u));
        session.getTransaction().commit();

        session = HibernateUtil.getSession();
        session.beginTransaction();
    }

    @After
    public void tearDown() throws Exception {
        if (session.getTransaction().isActive())
            session.getTransaction().commit();

        session = HibernateUtil.getSession();
        session.beginTransaction();
        session.delete(perm1);
        session.delete(perm2);
        session.delete(testBin);
        session.getTransaction().commit();
    }

    @Test
    public void testIsOwner() throws Exception {
        Assert.assertTrue(testBin.isOwner(testOwner));
        Assert.assertTrue(testBin.isOwner(new String(testOwner)));
    }

    @Test
    public void testCanDelete() throws Exception {
        Assert.assertFalse(testBin.canDelete(testBin.getToken()));
        Assert.assertFalse(testBin.canDelete("fake token"));
    }

    @Test
    public void testCanSeeAll() throws Exception {
        Assert.assertTrue(testBin.canSeeAll(testOwner));
        Assert.assertFalse(testBin.canSeeAll(randomUser));
    }

    @Test
    public void testCanAddSubmission() throws Exception {
        Assert.assertTrue(testBin.canAddSubmission(perm1u));
        Assert.assertTrue(testBin.canAddSubmission(perm2u));
        Assert.assertFalse(testBin.canAddSubmission(testOwner));
        Assert.assertFalse(testBin.canAddSubmission(randomUser));
    }

    @Test
    public void testCanSeeSubmission() throws Exception {


    }

    @Test
    public void testCanDeleteSubmission() throws Exception {

    }

    @Test
    public void testCanAddPermission() throws Exception {

    }

    @Test
    public void testCanDeletePermission() throws Exception {

    }
}