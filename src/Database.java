import java.sql.*;

public class Database {

    private static final String DATABASE_URL = "jdbc:mysql://localhost:3306/myCafeteriaDB";
    private static final String DATABASE_USER = "root";
    private static final String DATABASE_PASSWORD = "daksh0915@";

    // Method to get a database connection
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DATABASE_URL, DATABASE_USER, DATABASE_PASSWORD);
    }

    // Method to get the hashed password for a given username
    public static String getUserPasswordHash(String username) {
        String passwordHash = null;
        String query = "SELECT password_hash FROM users WHERE username = ?";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, username);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    passwordHash = rs.getString("password_hash");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace(); // Handle the exception properly
        }

        return passwordHash;
    }
}
