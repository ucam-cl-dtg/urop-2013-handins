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
        request.setAttribute("markable", true);
        request.setAttribute("archived", false);

        return Util.forward(request, "/api/bins/");
    }

    /*
    Done
     */
    @GET
    @Path("/upload")
    public Object viewBinsToUpload() {
        request.setAttribute("archived", false);

        return Util.forward(request, "/api/bins/");
    }


    /*
    Done
     */
    @GET
    @Path("/manage")
    public Object viewBinsOwned() {
        request.setAttribute("owner", getCurrentUser());
        request.setAttribute("archived", false);

        return Util.forward(request, "/api/bins/");
    }
}
