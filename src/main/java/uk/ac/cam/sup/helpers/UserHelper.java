package uk.ac.cam.sup.helpers;

import javax.servlet.http.HttpServletRequest;

public class UserHelper {

    public static boolean isAdmin(String user) {
        return user == null;
    }

    public static String getCurrentUser(HttpServletRequest req) {
        return (String) req.getAttribute("userId");
    }
}
