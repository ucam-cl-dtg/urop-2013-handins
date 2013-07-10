package uk.ac.cam.sup.controllers;

import com.google.common.collect.ImmutableMap;
import com.googlecode.htmleasy.ViewWith;
import org.eclipse.jetty.http.HttpFields;
import org.hibernate.Session;
import uk.ac.cam.sup.HibernateUtil;
import uk.ac.cam.sup.models.Bin;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

@Path("/bin")
public class BinController {


    @POST
    @Path("")
    @Produces("application/json")
    public Map<String, ?> createBin(@FormParam("owner") String owner,
                                    @FormParam("question") String question ) {
        Session session = HibernateUtil.getSession();

        Bin bin = new Bin(owner, question);
        session.save(bin);

        return ImmutableMap.of("id", bin.getId(),
                               "token", bin.getToken());
    }

    // TODO Remove this. It is a security leak. It is just used for devel
    @GET
    @Path("")
    @Produces("application/json")
    public List<Map<String, ?>> listBins() {
        Session session = HibernateUtil.getSession();

        List<Bin> l = session.createCriteria(Bin.class).list();
        List<Map<String, ?> > res = new LinkedList<Map<String, ?> >();
        for (Bin bin: l) {

            Map<String,?> obj = ImmutableMap.of("id", bin.getId(),
                                    "token", bin.getToken(),
                                    "question", bin.getQuestion(),
                                    "owner", bin.getOwner());
            res.add(obj);
        }
        return res;
    }

}
