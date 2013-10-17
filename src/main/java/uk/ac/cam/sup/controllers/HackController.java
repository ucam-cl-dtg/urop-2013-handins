package uk.ac.cam.sup.controllers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.cam.cl.dtg.ldap.LDAPObjectNotFoundException;
import uk.ac.cam.cl.dtg.ldap.LDAPPartialQuery;
import uk.ac.cam.cl.dtg.teaching.api.QuestionsApi;
import uk.ac.cam.sup.helpers.ArrayHelper;

import javax.ws.rs.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Path("/hack")
public class HackController extends ApplicationController {
    private static Logger log = LoggerFactory.getLogger(HackController.class);

    /*
    This hack was used for logging by force into different users

    @Path("/user")
    @GET
    @ViewWith("/soy/handins.hack.user")
    public Object showHackUser(){
        return ImmutableMap.of("user", getCurrentUser());
    }

    @Path("/user")
    @POST
    public void hackUser(@FormParam("user") String user) {
        request.getSession().setAttribute("RavenRemoteUser", user);
        throw new RedirectException("/hack/user");
    }
    */


    private List<HashMap<String, Object>> searchByCrsid(String query) {
    	try	{
    		return LDAPPartialQuery.partialUserByCrsid(query);
        } catch (LDAPObjectNotFoundException e){
            log.error("Error performing LDAPQuery: " + e.getMessage());
            return new ArrayList<HashMap<String,Object>>();
        }
    }

    private List<HashMap<String, Object>> searchBySurname(String query) {
        try {
            return LDAPPartialQuery.partialUserBySurname(query);
        } catch (LDAPObjectNotFoundException e){
            log.error("Error performing LDAPQuery: " + e.getMessage());
            return new ArrayList<HashMap<String,Object>>();
        }
    }

    @Path("/users")
    @GET
    @Produces("application/json")
    public Object listUsers(@QueryParam("term") String query) {
        System.out.println("Query: >>>" + query + "<<<");
        // Perform LDAP search
        List<HashMap<String, Object>> m1 = searchByCrsid(query);
        List<HashMap<String, Object>> m2 = searchBySurname(query);

        List<HashMap<String, Object>> matches = ArrayHelper.interleave(m1, m2, 15);
        return matches;
    }

    @Path("/tester")
    @GET
    @Produces("application/json")
    public Object testApi() {
        String apiKey = getRequest().getSession().getServletContext().getInitParameter("apiKey");
        /*
        log.error(apiKey);
        HandinsApi.HandinsApiWrapper api = new HandinsApi.HandinsApiWrapper("http://localhost:8080/handins", apiKey);

        HandinsApi.Bin bin = api.createBin("Api merge", "at628");
        String[] users = new String[] {"at628", "ap760", "igs23"};
        api.setUsers(bin, users);
        */

        QuestionsApi.QuestionsApiWrapper api = new QuestionsApi.QuestionsApiWrapper("http://localhost:8080/questions", apiKey);
        return api.getQuestionSet(1, "at628");

    }
}
