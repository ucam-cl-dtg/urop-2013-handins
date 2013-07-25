package uk.ac.cam.sup.controllers;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;

@Path("/marking")
public class MarkingListings {
    @Context
    private HttpServletRequest request;

    @GET
    @Produces("application/json")
    public Object showBinsToMark() {
        return (new BinController()).viewBinList();
    }
}
