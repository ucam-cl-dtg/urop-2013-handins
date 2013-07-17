package uk.ac.cam.sup.controllers;

import com.google.common.collect.ImmutableMap;
import com.googlecode.htmleasy.ViewWith;

import javax.ws.rs.GET;
import javax.ws.rs.Path;

@Path("/testx")
public class TestUploadAnnotatedWorkController {
    @GET
    @ViewWith("/soy/main.indexx")
    public Object bla(){
        return ImmutableMap.of();
    }
}
