package dbms_project.database;

import java.sql.*;

import io.github.cdimascio.dotenv.Dotenv;

public class JDBCConnectivity {
    static Dotenv dotenv = Dotenv.load();

    private static final String URL = dotenv.get("DB_URL");
    private static final String USER = dotenv.get("DB_USER");
    private static final String PASSWORD = dotenv.get("DB_PASSWORD");


    public static Connection getConnection() {
        Connection connection = null;
        try {
            // Registering the driver
            Class.forName("com.mysql.cj.jdbc.Driver");

            // Establishing the connection
            connection = DriverManager.getConnection(URL, USER, PASSWORD);
            System.out.println("Connection successful!");
        } catch (ClassNotFoundException e) {
            System.out.println("MySQL Driver not found.");
            e.printStackTrace();
        } catch (SQLException e) {
            System.out.println("Connection failed!");
            e.printStackTrace();
        }
        return connection;
    }

    public static String getDatabaseUserType(String email){
        String sql = "SELECT User_Type FROM user WHERE Email = ?";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, email);

            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getString("User_Type");
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    public static int getDatabaseUserID(String email) {
        String sql = "SELECT User_ID FROM `user` WHERE Email = ?";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, email);

            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getInt("User_ID");
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return -1;
    }

}


