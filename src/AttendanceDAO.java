import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AttendanceDAO {
    private String jdbcURL = "jdbc:mysql://localhost:3306/myCafeteriaDB";
    private String jdbcUsername = "root";
    private String jdbcPassword = "daksh0915@";

    protected Connection getConnection() throws SQLException {
        return DriverManager.getConnection(jdbcURL, jdbcUsername, jdbcPassword);
    }

    public boolean recordAttendance(int staffId, Date date, Time checkInTime, Time checkOutTime) throws SQLException {
        String insertAttendanceSQL = "INSERT INTO staff_attendance (staff_id, attendance_date, check_in_time, check_out_time) VALUES (?, ?, ?, ?);";
        try (Connection connection = getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(insertAttendanceSQL)) {
            preparedStatement.setInt(1, staffId);
            preparedStatement.setDate(2, date);
            preparedStatement.setTime(3, checkInTime);
            preparedStatement.setTime(4, checkOutTime);

            int affectedRows = preparedStatement.executeUpdate();
            return affectedRows > 0;
        }
    }

    public List<AttendanceRecord> getAttendanceRecordsByStaffId(int staffId) throws SQLException {
        List<AttendanceRecord> records = new ArrayList<>();
        String selectSQL = "SELECT sa.attendance_id, r.username, sa.attendance_date, sa.check_in_time, sa.check_out_time "
                + "FROM staff_attendance sa "
                + "JOIN reg r ON sa.staff_id = r.id "
                + "WHERE sa.staff_id = ? "
                + "ORDER BY sa.attendance_date DESC;";

        try (Connection connection = getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(selectSQL)) {
            preparedStatement.setInt(1, staffId);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    int attendanceId = resultSet.getInt("attendance_id");
                    String username = resultSet.getString("username"); // This should work now
                    Date date = resultSet.getDate("attendance_date");
                    Time checkInTime = resultSet.getTime("check_in_time");
                    Time checkOutTime = resultSet.getTime("check_out_time");
                    records.add(new AttendanceRecord(attendanceId, username, date, checkInTime, checkOutTime));
                }
            }
        }

        return records;
    }



    // New method to get attendance records for all staff
    public List<AttendanceRecord> getAllAttendanceRecords() throws SQLException {
        List<AttendanceRecord> records = new ArrayList<>();
        // Adjusted SQL query to join with the reg table and select the username
        String selectAllSQL = "SELECT sa.attendance_id, r.username, sa.attendance_date, sa.check_in_time, sa.check_out_time "
                + "FROM staff_attendance sa "
                + "JOIN reg r ON sa.staff_id = r.id "
                + "ORDER BY sa.attendance_date DESC, r.username ASC;";

        try (Connection connection = getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(selectAllSQL);
             ResultSet resultSet = preparedStatement.executeQuery()) {

            while (resultSet.next()) {
                int attendanceId = resultSet.getInt("attendance_id");
                String username = resultSet.getString("username"); // Get username
                Date date = resultSet.getDate("attendance_date");
                Time checkInTime = resultSet.getTime("check_in_time");
                Time checkOutTime = resultSet.getTime("check_out_time");
                // Adjusted to pass username instead of staffId
                records.add(new AttendanceRecord(attendanceId, username, date, checkInTime, checkOutTime));
            }
        }

        return records;
    }

}