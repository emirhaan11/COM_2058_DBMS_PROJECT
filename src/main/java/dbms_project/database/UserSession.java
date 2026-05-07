package dbms_project.database;

public class UserSession {
    private static int userId;
    private static String userType;

    public static void setSession(int id, String type) {
        userId = id;
        userType = type;
    }
    public static int getUserId() { return userId; }
    public static String getUserType() { return userType; }
}