package projek_keuangan.manager;

import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

public class SessionManager {
    private static final String PREF_NODE_NAME = "projek_keuangan_prefs";
    private static final String KEY_LOGGED_IN_USER = "loggedInUser";
    private static Preferences prefs;

    static {
        prefs = Preferences.userRoot().node(PREF_NODE_NAME);
    }

    public static void loginUser(String username) {
        if (username == null) {
            prefs.remove(KEY_LOGGED_IN_USER);
        } else {
            prefs.put(KEY_LOGGED_IN_USER, username);
        }
        try {
            prefs.flush();
        } catch (BackingStoreException e) {
            System.err.println("Error flushing preferences during login: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static String getLoggedInUser() {
        return prefs.get(KEY_LOGGED_IN_USER, null);
    }

    public static void logoutUser() {
        prefs.remove(KEY_LOGGED_IN_USER);
        try {
            prefs.clear();
            prefs.flush();
        } catch (BackingStoreException e) {
            System.err.println("Error flushing preferences during logout: " + e.getMessage());
            e.printStackTrace();
        }
    }
}