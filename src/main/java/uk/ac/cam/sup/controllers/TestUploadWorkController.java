package uk.ac.cam.sup.controllers;

import com.google.common.collect.ImmutableMap;
import com.googlecode.htmleasy.ViewWith;

import javax.ws.rs.GET;
import javax.ws.rs.Path;

@Path("/test")
public class TestUploadWorkController {
    @GET
    @ViewWith("/soy/main.index")
    public Object bla(){
        return ImmutableMap.of();
    }
}
