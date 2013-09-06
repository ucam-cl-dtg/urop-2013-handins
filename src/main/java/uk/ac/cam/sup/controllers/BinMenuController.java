package uk.ac.cam.sup.controllers;

import com.google.common.collect.ImmutableMap;
import org.jboss.resteasy.annotations.Form;
import uk.ac.cam.sup.helpers.Util;
import uk.ac.cam.sup.models.Bin;
import uk.ac.cam.sup.queries.BinQuery;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

@Path("/bins")
@Produces("application/json")
public class BinMenuController extends ApplicationController {

    HttpServletRequest request = Util.getRequest();

    @GET
    @Path("/")
    public Object listBins(@Form BinQuery query) {
        query.init();

        return ImmutableMap.of(
                "bins", Bin.toJSON(query.fetch()),
                "meta", ImmutableMap.of(
                        "count", query.count(),
                        "limit", query.getLimit(),
                        "offset", query.getOffset()
                )
        );

    }


    @GET
    @Path("/dos")
    public Object dosView() {
        return Util.forward(request, "/api/bins/");
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
