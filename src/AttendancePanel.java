import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.sql.SQLException;
import java.sql.Time;
import java.util.Date;
import java.util.List;
import java.awt.event.ActionListener;
public class AttendancePanel extends JPanel {
    private JTable attendanceTable;
    private JButton checkInButton, checkOutButton;
    private AttendanceDAO attendanceDAO;
    private int staffId; // This should be set to the current staff's ID
    private StaffDashboard staffDashboard;
    public AttendancePanel(int staffId, StaffDashboard staffDashboard) {
        this.staffId = staffId;
        this.attendanceDAO = new AttendanceDAO();
        this.staffDashboard = staffDashboard;
        setLayout(new BorderLayout());
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        JButton backButton = createBackButton();
        bottomPanel.add(backButton);
        add(bottomPanel, BorderLayout.SOUTH);
        add(createAttendanceControls(), BorderLayout.NORTH);
        add(new JScrollPane(createAttendanceTable()), BorderLayout.CENTER);
        refreshAttendanceData();
    }
    private JButton createBackButton() {
        JButton backButton = new JButton("Back to Dashboard");
        backButton.addActionListener(e -> {
            if (staffDashboard != null) {
                this.setVisible(false); // Hide the attendance panel
                staffDashboard.showMainPanel(); // Make sure the staffDashboard is not null
            } else {
                System.err.println("StaffDashboard reference is null.");
            }
        });
        return backButton;
    }

    private JPanel createAttendanceControls() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        checkInButton = new JButton("Check In");
        checkOutButton = new JButton("Check Out");

        checkInButton.addActionListener(e -> recordCheckIn());
        checkOutButton.addActionListener(e -> recordCheckOut());

        panel.add(checkInButton);
        panel.add(checkOutButton);

        return panel;
    }

    private JTable createAttendanceTable() {
        // Columns for the attendance table
        String[] columns = new String[]{"Date", "Check In", "Check Out"};
        DefaultTableModel model = new DefaultTableModel(columns, 0);
        attendanceTable = new JTable(model);
        return attendanceTable;
    }

    private void recordCheckIn() {
        // Logic to record the check-in time
        try {
            boolean success = attendanceDAO.recordAttendance(staffId, new java.sql.Date(new Date().getTime()), new Time(new Date().getTime()), null);
            if (success) {
                JOptionPane.showMessageDialog(this, "Check-in recorded successfully.");
                refreshAttendanceData();
            } else {
                JOptionPane.showMessageDialog(this, "Failed to record check-in.");
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Database error occurred while recording check-in.");
        }
    }

    private void recordCheckOut() {
        // Logic to record the check-out time
        try {
            boolean success = attendanceDAO.recordAttendance(staffId, new java.sql.Date(new Date().getTime()), null, new Time(new Date().getTime()));
            if (success) {
                JOptionPane.showMessageDialog(this, "Check-out recorded successfully.");
                refreshAttendanceData();
            } else {
                JOptionPane.showMessageDialog(this, "Failed to record check-out.");
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Database error occurred while recording check-out.");
        }
    }

    private void refreshAttendanceData() {
        // Fetch and display attendance data
        try {
            List<AttendanceRecord> records = attendanceDAO.getAttendanceRecordsByStaffId(staffId);
            DefaultTableModel model = (DefaultTableModel) attendanceTable.getModel();
            model.setRowCount(0); // Clear the table

            for (AttendanceRecord record : records) {
                model.addRow(new Object[]{
                        record.getDate(),
                        record.getCheckInTime(),
                        record.getCheckOutTime()
                });
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Database error occurred while fetching attendance records.");
        }
    }
}