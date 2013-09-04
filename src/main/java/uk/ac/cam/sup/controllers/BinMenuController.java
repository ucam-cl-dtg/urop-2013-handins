package uk.ac.cam.sup.controllers;

import com.google.common.collect.ImmutableMap;
import org.hibernate.Session;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import uk.ac.cam.sup.HibernateUtil;
import uk.ac.cam.sup.helpers.UserHelper;
import uk.ac.cam.sup.models.Bin;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

@Path("/bins")
@Produces("application/json")
public class BinMenuController extends ApplicationController {

    @GET
    @Path("/marking")
    public Object showBinsToMark() {

        // Set Hibernate and get user
        Session session = HibernateUtil.getSession();

        String user = getCurrentUser();

        // Get list of bins
        @SuppressWarnings("unchecked")
        List<Bin> binList = session.createCriteria(Bin.class)
                                   .createAlias("accessPermissions", "perm")
                                   .add(Restrictions.eq("perm.userCrsId", user))
                                   .addOrder(Order.desc("id"))
                                   .list();

        // Filter all visible bins and return them
        List<Map<String, ?>> finalBinList = new LinkedList<>();
        for (Bin bin : binList)
            if (bin.canAddMarkedSubmission(user))
                finalBinList.add(ImmutableMap.of("id", bin.getId(),
                                                 "name", bin.getName(),
                                                 "isArchived", bin.isArchived(),
                                                 "questions", bin.getQuestionCount()));

        return ImmutableMap.of("bins", finalBinList);
    }

    /*
    Done
     */
    @GET
    @Path("/upload")
    public Object viewBinsToUpload() {

        // Set Hibernate and get user
        Session session = HibernateUtil.getSession();

        String user = getCurrentUser();

        // Get list of bins
        @SuppressWarnings("unchecked")
        List<Bin> binList = session.createCriteria(Bin.class)
                                   .createAlias("accessPermissions", "perm")
                                   .add(Restrictions.eq("perm.userCrsId", user))
                                   .addOrder(Order.desc("id"))
                                   .list();

        // Filter all visible bins and return them
        List<Map<String, ?>> finalBinList = new LinkedList<>();
        for (Bin bin : binList)
            finalBinList.add(ImmutableMap.of("id", bin.getId(),
                                             "name", bin.getName(),
                                             "isArchived", bin.isArchived(),
                                             "questions", bin.getQuestionCount()));

        return ImmutableMap.of("bins", finalBinList);
    }


    /*
    Done
     */
    @GET
    @Path("/manage")
    public Object viewBinsOwned() {

        // Set Hibernate and get user
        Session session = HibernateUtil.getSession();

        String user = getCurrentUser();

        // Get list of bins
        @SuppressWarnings("unchecked")
        List<Bin> binList = session.createCriteria(Bin.class)
                                   .add(Restrictions.eq("owner", user))
                                   .addOrder(Order.desc("id"))
                                   .list();

        // Filter all visible bins and return them
        List<Map<String, ?>> finalBinList = new LinkedList<>();
        for (Bin bin : binList)
            finalBinList.add(ImmutableMap.of("id", bin.getId(),
                                             "name", bin.getName(),
                                             "isArchived", bin.isArchived(),
                                             "questions", bin.getQuestionCount()));

        return ImmutableMap.of("bins", finalBinList);
    }
}
