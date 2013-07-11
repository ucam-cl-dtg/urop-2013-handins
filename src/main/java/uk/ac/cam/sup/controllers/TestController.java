package uk.ac.cam.sup.controllers;

import com.google.common.collect.ImmutableMap;
import com.googlecode.htmleasy.ViewWith;

import javax.ws.rs.GET;
import javax.ws.rs.Path;

/**
 * Created with IntelliJ IDEA.
 * User: ap760
 * Date: 11/07/13
 * Time: 12:45
 * To change this template use File | Settings | File Templates.
 */
@Path("/")
public class TestController {
    @GET
    @Path("/test")
    @ViewWith("/soy/main.index")
    public Object bla(){
        return ImmutableMap.of();
    }
}
