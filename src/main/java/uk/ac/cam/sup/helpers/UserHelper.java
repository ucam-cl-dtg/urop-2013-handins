package uk.ac.cam.sup.helpers;

public class UserHelper {

    public static String currentUser = "ap760";

    public static boolean isDos(String user) {
        return false;
    }

    public static boolean isAdmin(String user) {
        return false;
    }

    public static String getCurrentUser() {
        return currentUser;
    }
}
