package uk.ac.cam.sup.controllers;

import com.google.common.collect.ImmutableMap;
import com.googlecode.htmleasy.ViewWith;
import uk.ac.cam.sup.helpers.UserHelper;

import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;

@Path("/")
public class login {
    @GET
    public Object bla(@FormParam("crsId") String crsId) {
        UserHelper.currentUser = crsId;

        return Response.ok().build();
    }
}
