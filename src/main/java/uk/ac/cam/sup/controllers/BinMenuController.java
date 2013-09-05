package uk.ac.cam.sup.controllers;

import com.google.common.collect.ImmutableMap;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.jboss.resteasy.annotations.Form;
import uk.ac.cam.sup.HibernateUtil;
import uk.ac.cam.sup.helpers.Util;
import uk.ac.cam.sup.models.Bin;
import uk.ac.cam.sup.queries.BinQuery;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

@Path("/bins")
@Produces("application/json")
public class BinMenuController extends ApplicationController {

    HttpServletRequest request = Util.getRequest();

    @GET
    @Path("/")
    public Object listBins(@Form BinQuery query) {
        query.init();

        return ImmutableMap.of("bins", Bin.toJSON(query.fetch()));
    }


    @GET
    @Path("/marking")
    public Object showBinsToMark() {
        //request.setAttribute("markable", true);
        request.setAttribute("name", "ana");

        return Util.forward(request, "/api/bins/");
        //return searchBins(null, null, false, null, null, true);
    }

    /*
    Done
     */
    @GET
    @Path("/upload")
    public Object viewBinsToUpload() {
        return searchBins(null, null, false, null, null, null);
    }


    /*
    Done
     */
    @GET
    @Path("/manage")
    public Object viewBinsOwned() {
        return searchBins(null, getCurrentUser(), false, null, null, null);
    }

    /*
    Done
     */
    @GET
    @Path("/search")
    public Object searchBins(@QueryParam("name") String queryName,
                             @QueryParam("owner") String queryOwner,
                             @QueryParam("archived") Boolean queryArchived,
                             @QueryParam("offset") Integer queryOffset,
                             @QueryParam("limit") Integer queryLimit,
                             @QueryParam("markable") Boolean queryMarkable) {

        // Set Hibernate and get user
        Session session = HibernateUtil.getSession();

        String user = getCurrentUser();

        // Get list of bins
        @SuppressWarnings("unchecked")
        Criteria binCriteria = session.createCriteria(Bin.class)
                                      .createAlias("accessPermissions", "perm")
                                      .add(Restrictions.eq("perm.userCrsId", user))
                                      .addOrder(Order.desc("id"));

        // ToDo: fuzzySearch maybe?
        if (queryName != null)
            binCriteria.add(Restrictions.eq("name", queryName));

        if (queryArchived != null)
            binCriteria.add(Restrictions.eq("isArchived", queryArchived));
        if (queryOwner != null)
            binCriteria.add(Restrictions.eq("owner", queryOwner));
        if (queryOffset != null)
            binCriteria.setFirstResult(queryOffset);
        if (queryLimit != null)
            binCriteria.setMaxResults(queryLimit);

        // Get list of bins
        @SuppressWarnings("unchecked")
        List<Bin> binList = binCriteria.list();

        // Filter all visible bins and return them
        List<Map<String, ?>> finalBinList = new LinkedList<>();
        for (Bin bin : binList)
            if (queryMarkable == null || (queryMarkable && bin.canAddMarkedSubmission(user)))
                finalBinList.add(ImmutableMap.of("id", bin.getId(),
                                                 "name", bin.getName(),
                                                 "isArchived", bin.isArchived(),
                                                 "questions", bin.getQuestionCount()));

        return ImmutableMap.of("bins", finalBinList);
    }
}
