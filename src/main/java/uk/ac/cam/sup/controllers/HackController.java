package uk.ac.cam.sup.controllers;

import com.google.common.collect.ImmutableMap;
import com.googlecode.htmleasy.RedirectException;
import com.googlecode.htmleasy.ViewWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.cam.cl.dtg.ldap.LDAPObjectNotFoundException;
import uk.ac.cam.cl.dtg.ldap.LDAPPartialQuery;
import uk.ac.cam.sup.helpers.ArrayHelper;
import uk.ac.cam.sup.helpers.UserHelper;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Path("/hack")
public class HackController {
    private static Logger log = LoggerFactory.getLogger(HackController.class);

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


    private List<HashMap<String, String>> searchByCrsid(String query) {
        List<HashMap<String, String>> matches;
        try {
            matches = LDAPPartialQuery.partialUserByCrsid(query);
        } catch (LDAPObjectNotFoundException e){
            log.error("Error performing LDAPQuery: " + e.getMessage());
            return new ArrayList<>();
        }
        return matches;
    }

    private List<HashMap<String, String>> searchBySurname(String query) {
        List<HashMap<String, String>> matches;
        try {
            matches = LDAPPartialQuery.partialUserBySurname(query);
        } catch (LDAPObjectNotFoundException e){
            log.error("Error performing LDAPQuery: " + e.getMessage());
            return new ArrayList<>();
        }
        return matches;
    }

    @Path("/users")
    @GET
    @Produces("application/json")
    public Object listUsers(@QueryParam("term") String query) {
        System.out.println("Query: >>>" + query + "<<<");
        // Perform LDAP search
        List m1 = searchByCrsid(query);
        List m2 = searchBySurname(query);

        List matches = ArrayHelper.interleave(m1, m2, 15);
        System.out.println("------------------- crsid -----------");
        System.out.println(m1);
        System.out.println("--------------------Surname ---------");
        System.out.println(m2);


        return matches;
    }
}
