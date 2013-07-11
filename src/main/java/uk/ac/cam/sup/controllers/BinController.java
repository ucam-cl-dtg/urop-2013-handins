package uk.ac.cam.sup.controllers;

import com.google.common.collect.ImmutableMap;
import com.googlecode.htmleasy.ViewWith;
import org.eclipse.jetty.http.HttpFields;
import org.hibernate.FetchMode;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;
import uk.ac.cam.sup.HibernateUtil;
import uk.ac.cam.sup.models.Bin;
import uk.ac.cam.sup.models.BinPermission;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import java.util.*;

@Path("/bin/")
@Produces("application/json")
public class BinController {


    private Bin getBin(long id) {
        Session session = HibernateUtil.getSession();
        Bin bin = (Bin) session.createCriteria(Bin.class)
                .add(Restrictions.eq("id", id))
                .setFetchMode("permissions", FetchMode.JOIN)
                .uniqueResult();
        return bin;
    }

    @POST
    @Path("")
    public Map<String, ?> createBin(@FormParam("owner") String owner,
                                    @FormParam("question") String question ) {
        Session session = HibernateUtil.getSession();

        Bin bin = new Bin(owner, question);
        session.save(bin);

        return ImmutableMap.of("id", bin.getId(),
                               "token", bin.getToken());
    }

    @GET
    @Path("{id}/permission/")
    public List listPermissions(@PathParam("id") long id) {
        Bin bin = getBin(id);

        if (bin == null)
            throw new NotFoundException();

        List res = new LinkedList();

        for (BinPermission binPermission: bin.getPermissions()) {
            res.add(binPermission.getUser());
        }

        return res;
    }


    @POST
    @Path("{id}/permission/")
    public Response addPermission(@PathParam("id") long id,
                              @FormParam("users") String[] users,
                              @FormParam("token") String token) {
        Bin bin = getBin(id);

        if (bin == null)
            throw new NotFoundException();

        if (!bin.canAddPermission(token))
            return Response.status(401).build();

        Set<String> usersWithPermission = new HashSet<String>();
        for (BinPermission perm: bin.getPermissions()) {
            usersWithPermission.add(perm.getUser());
        }

        for (String user: users) {
            if (usersWithPermission.contains(user))
                continue;
            bin.getPermissions().add(new BinPermission(bin, user));
        }

        return Response.ok().build();
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
