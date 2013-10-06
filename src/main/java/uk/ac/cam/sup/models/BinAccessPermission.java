package uk.ac.cam.sup.models;

import java.util.Date;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import uk.ac.cam.cl.dtg.ldap.LDAPObjectNotFoundException;
import uk.ac.cam.cl.dtg.ldap.LDAPQueryManager;
import uk.ac.cam.cl.dtg.teaching.api.DashboardApi.DashboardApiWrapper;
import uk.ac.cam.cl.dtg.teaching.hibernate.HibernateUtil;

@Entity
@Table(name = "BinAccessPermission")
public class BinAccessPermission {
    // Fields
    @Id
    @GeneratedValue(strategy=GenerationType.SEQUENCE, generator="binAccessPermissionIdSeq")
    @SequenceGenerator(name="binAccessPermissionIdSeq", sequenceName="BinAccessPermissionSEQ", allocationSize=1)
    private long id;

    private Date dateCreated;

    private String userCrsId;

    @ManyToOne
    private Bin bin;

    // Constructors
    public BinAccessPermission() {
        dateCreated = new Date();
    }

    public BinAccessPermission(Bin bin, String userCrsId, DashboardApiWrapper api) throws LDAPObjectNotFoundException {
        dateCreated = new Date();

        setBin(bin);
        setUserCrsId(userCrsId);

        org.hibernate.Session session = HibernateUtil.getInstance().getSession();

        List<String> insList = LDAPQueryManager.getUser(userCrsId).getInstID();
        for (String inst: insList) {
            List<String> doses = api.getDosesForInstitution(userCrsId, inst);

            for (String dos : doses)
                session.save(new BinDosAccess(bin, dos));
        }
    }

    // Id
    public long getId() {
        return id;
    }

    // Bin
    public Bin getBin() {
        return bin;
    }

    public void setBin(Bin bin) {
        this.bin = bin;
    }

    // UserCrsId
    public String getUserCrsId() {
        return userCrsId;
    }

    public void setUserCrsId(String userCrsId) {
        this.userCrsId = userCrsId;
    }

    // DateCreated
    public void setDateCreated(Date dateCreated) {
        this.dateCreated = dateCreated;
    }

    public Date getDateCreated() {
        return dateCreated;
    }
}
