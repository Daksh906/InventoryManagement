import java.sql.Time;
import java.sql.Date;

public class AttendanceRecord {
    private int attendanceId;
    private String username; // Changed from staffId to username
    private Date date;
    private Time checkInTime;
    private Time checkOutTime;

    public AttendanceRecord(int attendanceId, String username, Date date, Time checkInTime, Time checkOutTime) {
        this.attendanceId = attendanceId;
        this.username = username;
        this.date = date;
        this.checkInTime = checkInTime;
        this.checkOutTime = checkOutTime;
    }

    // Getters
    public int getAttendanceId() { return attendanceId; }
    public String getUsername() { return username; }
    public Date getDate() { return date; }
    public Time getCheckInTime() { return checkInTime; }
    public Time getCheckOutTime() { return checkOutTime; }

    // You may also want to add setters as needed
}
