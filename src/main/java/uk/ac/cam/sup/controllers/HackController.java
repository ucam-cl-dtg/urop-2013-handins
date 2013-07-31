package uk.ac.cam.sup.controllers;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.googlecode.htmleasy.RedirectException;
import com.googlecode.htmleasy.ViewWith;
import uk.ac.cam.sup.helpers.UserHelper;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
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

    @Path("/users")
    @POST
    @Produces("application/json")
    public Object listUsersPost() {
        return ImmutableList.of(
                ImmutableMap.of("crsid", "ap760", "name", "Andrei Purice"),
                ImmutableMap.of("crsid", "at628", "name", "Alexandru Tache")
        );
    }

    @Path("/users")
    @GET
    @Produces("application/json")
    public Object listUsers() {
        return ImmutableList.of(
                ImmutableMap.of("crsid", "ap760", "name", "Andrei Purice", "label", "ap760", "value", "ap760"),
                ImmutableMap.of("crsid", "at628", "name", "Alexandru Tache", "label", "at628", "value", "at628")
        );
    }
}
