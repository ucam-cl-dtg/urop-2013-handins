package uk.ac.cam.sup.controllers;

import com.google.common.collect.ImmutableMap;
import com.googlecode.htmleasy.RedirectException;
import com.googlecode.htmleasy.ViewWith;
import uk.ac.cam.sup.helpers.UserHelper;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;

@Path("/hack")
public class HackController {

    @Context
    HttpServletRequest request;

    @Path("/user")
    @GET
    @ViewWith("/soy/handins.hack.user")
    public Object showHackUser(){
        return ImmutableMap.of("user", UserHelper.getCurrentUser(request));
    }

    @Path("/user")
    @POST
    public void hackUser(@FormParam("user") String user) {
        request.getSession().setAttribute("RavenRemoteUser", user);
        throw new RedirectException("/hack/user");
    }
}
