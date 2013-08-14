package uk.ac.cam.sup.controllers;

import com.google.common.collect.ImmutableMap;
import com.googlecode.htmleasy.RedirectException;
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

@Produces("application/json")
public class BinMenuController {

    @SuppressWarnings({"UnusedDeclaration"})
    @Context
    private HttpServletRequest request;

    @GET
    @Path("/marking")
    public Object showBinsToMark() {
        throw new RedirectException("/bins");
    }

    /*
    Done

    Checked
     */
    @GET
    @Path("/bins")
    public Object viewBinList() {

        // Set Hibernate and get user
        Session session = HibernateUtil.getSession();

        String user = UserHelper.getCurrentUser(request);

        // Get list of bins
        @SuppressWarnings("unchecked")
        List<Bin> binList = session.createCriteria(Bin.class)
                                   .createAlias("accessPermissions", "perm")
                                   .add(Restrictions.eq("perm.userCrsId", user))
                                   .addOrder(Order.desc("id"))
                                   .list();

        // Filter all visible bins and return them
        List<Map<String, ?>> finalBinList = new LinkedList<Map<String, ?>>();
        for (Bin bin : binList)
            finalBinList.add(ImmutableMap.of("id", bin.getId(),
                                             "name", bin.getName(),
                                             "isArchived", bin.isArchived(),
                                             "questions", bin.getQuestionCount()));

        return ImmutableMap.of("bins", finalBinList);
    }
}
