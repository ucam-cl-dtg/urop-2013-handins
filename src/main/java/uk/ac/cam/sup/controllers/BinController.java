package uk.ac.cam.sup.controllers;

import com.google.common.collect.ImmutableMap;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import java.util.Map;

@Path("/bin")
public class BinController {

    @POST
    @Path("")
    @Produces("application/json")

    public Map<String, ?> createBin() {
        return ImmutableMap.of("ana", "mere");
    }

}
