package uk.ac.cam.sup.helpers;

import org.jboss.resteasy.spi.HttpRequest;

import javax.servlet.http.HttpServletRequest;

public class UserHelper {

    public static String currentUser = "ap760";

    public static boolean isDos(String user) {
        return false;
    }

    public static boolean isAdmin(String user) {
        return false;
    }

    public static String getCurrentUser(HttpServletRequest req) {
        return (String) req.getSession().getAttribute("RavenRemoteUser");
    }
}
