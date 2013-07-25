package uk.ac.cam.sup.controllers;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

@Path("/marking")
public class MarkingListings {
    @GET
    @Produces("application/json")
    public Object showBinsToMark() {
        return (new BinController()).viewBinList();
    }
}
