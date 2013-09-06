package uk.ac.cam.sup.queries;

import org.hibernate.Criteria;
import org.hibernate.ScrollMode;
import org.hibernate.ScrollableResults;
import org.hibernate.Session;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.hibernate.sql.JoinType;
import uk.ac.cam.sup.HibernateUtil;
import uk.ac.cam.sup.helpers.UserHelper;
import uk.ac.cam.sup.models.Bin;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class BinQuery {
    private static final Long DEFAULT_LIMIT = 10L;
    @QueryParam("name") private String name;
    @QueryParam("owner") private String owner;
    @QueryParam("archived") private Boolean archived;
    @QueryParam("offset") private Long offset;
    @QueryParam("limit") private Long limit;
    @QueryParam("markable") private Boolean markable;

    @QueryParam("datecreated_after") private String after_str;
                                     private Date after;
    @QueryParam("datecreated_before") private String before_str;
                                      private Date before;


    @Context HttpServletRequest request;

    private String currentUser;
    private Criteria criteria;
    
    public BinQuery() {
    }
    
    public void init() {
        if (limit == null)
            limit = DEFAULT_LIMIT;

        if (offset == null)
            offset = 0L;

        after = extractDate(after_str);
        before = extractDate(before_str);

        if (request.getAttribute("name") != null)
            name = (String) request.getAttribute("name");

        if (request.getAttribute("owner") != null)
            owner = (String) request.getAttribute("owner");

        if (request.getAttribute("archived") != null)
            archived = (Boolean) request.getAttribute("archived");

        if (request.getAttribute("offset") != null)
            offset = (Long) request.getAttribute("offset");

        if (request.getAttribute("limit") != null)
            limit = (Long) request.getAttribute("limit");

        if (request.getAttribute("markable") != null)
            markable = (Boolean) request.getAttribute("markable");

        if (request.getAttribute("datecreate_after") != null)
            after = (Date) request.getAttribute("datecreate_after");

        if (request.getAttribute("datecreate_before") != null)
            before = (Date) request.getAttribute("datecreate_before");

        currentUser = UserHelper.getCurrentUser(request);
    }

    @SuppressWarnings("unchecked")
    public List<Bin> fetch() {
        Session session = HibernateUtil.getTransaction();
        
        criteria = session.createCriteria(Bin.class)
                          .createAlias("accessPermissions", "perm", JoinType.LEFT_OUTER_JOIN)
                          .createAlias("dosAccess", "dos", JoinType.LEFT_OUTER_JOIN)
                          .add(Restrictions.or(Restrictions.eq("perm.userCrsId", currentUser),
                                               Restrictions.eq("dos.userCrsId", currentUser)))
                          .setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY)
                          .addOrder(Order.desc("id"));

        addName();
        addOwner();
        addArchived();
        addMarkable();
        addBefore();
        addAfter();
        addOffset();
        addLimit();
        
        return (List<Bin>) criteria.list();
    }

    public int count() {
        Session session = HibernateUtil.getTransaction();

        criteria = session.createCriteria(Bin.class)
                .createAlias("accessPermissions", "perm", JoinType.LEFT_OUTER_JOIN)
                .createAlias("dosAccess", "dos", JoinType.LEFT_OUTER_JOIN)
                .add(Restrictions.or(Restrictions.eq("perm.userCrsId", currentUser),
                                     Restrictions.eq("dos.userCrsId", currentUser)))
                .setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY)
                .addOrder(Order.desc("id"));

        addName();
        addOwner();
        addArchived();
        addMarkable();
        addBefore();
        addAfter();

        ScrollableResults scroll = criteria.scroll(ScrollMode.SCROLL_INSENSITIVE);
        scroll.last();
        return scroll.getRowNumber() + 1;
    }

    private void addBefore() {
        if (before != null)
            criteria.add(Restrictions.le("dateCreated", before));
    }

    private void addAfter() {
        if (after != null)
            criteria.add(Restrictions.ge("dateCreated", after));
    }

    private void addCount() {
    }

    
    private void addName() {
        if (name != null)
            criteria.add(Restrictions.ilike("name", name, MatchMode.ANYWHERE));
    }

    private void addOwner() {
        if (owner != null)
            criteria.add(Restrictions.eq("owner", owner));
    }
    
    private void addArchived() {
        if (archived != null)
            criteria.add(Restrictions.eq("isArchived", archived));
    }

    private void addMarkable() {
        if (markable != null && markable)
            criteria.createAlias("userMarkingPermissions", "userPerm")
                    .add(Restrictions.eq("userPerm.userCrsId", currentUser));
    }
    
    private void addOffset() {
        if (offset != null)
            criteria.setFirstResult(offset.intValue());
    }
    
    private void addLimit() {
        if (limit != null)
            criteria.setMaxResults(limit.intValue());
    }

    public Long getLimit() {
        return limit;
    }

    public Long getOffset() {
        return offset;
    }

    private Date extractDate(String str) {
        if (str == null)
            return null;
        Date result = new Date();
        Calendar c = Calendar.getInstance();
        try {
            String[] split = str.split("/");
            int day = Integer.parseInt(split[0]);
            int month = Integer.parseInt(split[1]);
            int year = Integer.parseInt(split[2]);
            c.set(year, month-1, day, 0, 0, 0);
            result = c.getTime();
        } catch(Exception e) {
            return null;
        }
        return result;
    }
}
