package uk.ac.cam.sup.controllers;

import com.google.common.collect.ImmutableMap;
import org.hibernate.FetchMode;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;
import uk.ac.cam.sup.HibernateUtil;
import uk.ac.cam.sup.helpers.UserHelper;
import uk.ac.cam.sup.models.Bin;
import uk.ac.cam.sup.models.BinPermission;

import javax.ws.rs.*;
import javax.ws.rs.core.Response;
import java.util.*;

@Path("/bin")
@Produces("application/json")
public class BinController {

    public static Bin getBin(long id) {
        // Set Hibernate
        Session session = HibernateUtil.getSession();

        return (Bin) session.createCriteria(Bin.class)
                            .add(Restrictions.eq("id", id))
                            .setFetchMode("permissions", FetchMode.JOIN)
                            .uniqueResult();
    }

    @GET
    public Object listBins() {

        // Set Hibernate and get user
        Session session = HibernateUtil.getSession();

        String user = UserHelper.getCurrentUser();

        List<Bin> binList = session.createCriteria(Bin.class).list();
        List<Map<String, ?>> finalBinList = new LinkedList<Map<String, ?>>();

        for (Bin bin : binList)
            if (bin.canAddSubmission(user))
                finalBinList.add(ImmutableMap.of("id", bin.getId(),
                                                 "name", bin.getName(),
                                                 "isArchived", bin.isArchived()));

        return ImmutableMap.of("bins", finalBinList);
    }

    @POST
    public Map<String, ?> createBin(@FormParam("owner") String owner,
                                    @FormParam("questionSet") String questionSet ) {

        // Set Hibernate
        Session session = HibernateUtil.getSession();

        Bin bin = new Bin(owner, questionSet);
        session.save(bin);

        return ImmutableMap.of("id", bin.getId(),
                               "token", bin.getToken());
    }

    @POST
    @Path("/{binId}")
    public Object changeArchiveBin(@FormParam("owner") long binId) {

        // Set Hibernate and get user
        Session session = HibernateUtil.getSession();

        String user = UserHelper.getCurrentUser();

        Bin bin = (Bin) session.get(Bin.class, binId);

        if (bin.isOwner(user))
            bin.setArchived(!bin.isArchived());

        return Response.ok().build();
    }

    @DELETE
    @Path("/{id}/")
    public Response deleteBin(@PathParam("id") long id,
                              @QueryParam("token") String token) {

        Bin bin = getBin(id);

        if (bin == null)
            return Response.status(404).build();
        if (!bin.canDelete(token)) {
            return Response.status(401).build();
        }

        Session session = HibernateUtil.getSession();
        session.delete(bin);
        return Response.ok().build();
    }

    @GET
    @Path("/{id}")
    public Object showBin(@PathParam("id") long id) {
        Bin bin = getBin(id);

        if (bin == null)
            throw new NotFoundException();

        return ImmutableMap.of("bin", ImmutableMap.of(
                               "id", bin.getId(),
                               "name", bin.getName()));
    }

    @GET
    @Path("/{id}/permission/")
    public List<String> listPermissions(@PathParam("id") long id) {
        Bin bin = getBin(id);

        if (bin == null)
            throw new NotFoundException();

        List<String> res = new LinkedList<String>();

        for (BinPermission binPermission: bin.getPermissions()) {
            res.add(binPermission.getUser());
        }

        return res;
    }

    @POST
    @Path("/{id}/permission/")
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
    @Path("/{id}/permission")
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
}
