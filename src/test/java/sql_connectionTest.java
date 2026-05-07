import java.sql.*;
import dbms_project.database.JDBCConnectivity;

public class sql_connectionTest {
    static void main() {
        Connection conn = JDBCConnectivity.getConnection();

        if (conn != null){
            try {
                // Prepare sql query
                String sql = "SELECT * FROM JOBSEEKER";

                // Create statement
                Statement statement = conn.createStatement();

                // Take the request
                ResultSet resultSet = statement.executeQuery(sql);

                while (resultSet.next()){
                    System.out.println("SSn: "+resultSet.getInt("Ssn"));
                }
                resultSet.close();
                statement.close();
                conn.close();

            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

    }
}
