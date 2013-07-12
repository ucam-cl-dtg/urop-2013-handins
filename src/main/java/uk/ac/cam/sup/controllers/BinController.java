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

    public static Bin getBin(long id) {
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
                                    @FormParam("questionSet") String questionSet ) {
        Session session = HibernateUtil.getSession();

        Bin bin = new Bin(owner, questionSet);
        session.save(bin);

        return ImmutableMap.of("id", bin.getId(),
                               "token", bin.getToken());
    }

    @DELETE
    @Path("{id}/")
    public Response deleteBin(@PathParam("id") long id,
                              @QueryParam("token") String token) {
        Bin bin = getBin(id);
        if (bin == null)
            return Response.status(404).build();
        if (! bin.canDelete(token)) {
            return Response.status(401).build();
        }

        Session session = HibernateUtil.getSession();
        session.delete(bin);
        return Response.ok().build();
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
    public Response addPermissions(@PathParam("id") long id,
                              @FormParam("users[]") String[] users,
                              @FormParam("token") String token) {
        Bin bin = getBin(id);

        if (bin == null)
            return Response.status(404).build();

        if (!bin.canAddPermission(token))
            return Response.status(401).build();

        Set<String> usersWithPermission = new HashSet<String>();
        for (BinPermission perm: bin.getPermissions()) {
            usersWithPermission.add(perm.getUser());
        }

        Session session = HibernateUtil.getSession();
        for (String user: users) {
            if (usersWithPermission.contains(user))
                continue;
            session.save(new BinPermission(bin, user));
        }

        return Response.ok().build();
    }

    @DELETE
    @Path("{id}/permission")
    public Response removePermissions(@PathParam("id") long id,
                                      @QueryParam("users[]") String[] users,
                                      @QueryParam("token") String token) {
        Bin bin = getBin(id);

        if (bin == null)
            return Response.status(404).build();

        if (!bin.canDeletePermission(token))
            return Response.status(401).build();

        Session session = HibernateUtil.getSession();

        List permissions = session.createCriteria(BinPermission.class)
                                  .add(Restrictions.in("user", users))
                                  .add(Restrictions.eq("bin", bin))
                                  .list();

        for (Object perm: permissions) {
            session.delete(perm);
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
                                    "questionSet", bin.getQuestionSet(),
                                    "owner", bin.getOwner());
            res.add(obj);
        }
        return res;
    }


}
