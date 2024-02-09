import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.sql.SQLException;
import java.util.List;
import java.util.regex.PatternSyntaxException;
import javax.swing.RowFilter;
import java.util.ArrayList;

public class AdminDashboard extends JPanel {
    private InventoryDAO inventoryDAO;
    private SalesDAO salesDAO;
    private JFrame frame;
    private AttendanceDAO attendanceDAO;
    private JTable attendanceTable;
    private JTextField filterTextField;

    private JTextField filterUsernameTextField;
    private JTextField filterDateTextField;
    public AdminDashboard(JFrame frame) {
        this.frame = frame;
        frame.setTitle("Admin Dashboard");
        frame.setSize(800, 600);
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        inventoryDAO = new InventoryDAO();
        salesDAO = new SalesDAO(inventoryDAO);
        attendanceDAO = new AttendanceDAO();
        setLayout(new BorderLayout());

        JLabel welcomeLabel = new JLabel("Welcome, Admin!");
        welcomeLabel.setFont(new Font("Helvetica", Font.BOLD, 24));
        welcomeLabel.setHorizontalAlignment(JLabel.CENTER);

        JPanel adminFeaturesPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JButton salesButton = createSalesButton();
        JButton purchasesButton = createPurchasesButton();
        JButton viewAllAttendanceButton = new JButton("View All Attendance");
        viewAllAttendanceButton.setPreferredSize(new Dimension(200, 100));
        viewAllAttendanceButton.addActionListener(e -> showAllAttendancePanel());
        adminFeaturesPanel.add(salesButton);
        adminFeaturesPanel.add(purchasesButton);
        adminFeaturesPanel.add(viewAllAttendanceButton);

        JButton logoutButton = new JButton("Logout");
        logoutButton.addActionListener(e -> frame.dispose());

        add(welcomeLabel, BorderLayout.NORTH);
        add(adminFeaturesPanel, BorderLayout.CENTER);
        add(logoutButton, BorderLayout.SOUTH);
    }
    private JButton createSalesButton() {
        JButton salesButton = new JButton("Sales");
        salesButton.setPreferredSize(new Dimension(200, 100));
        salesButton.addActionListener(e -> {
            SalesPanel salesPanel = new SalesPanel(salesDAO, inventoryDAO, frame);
            frame.setContentPane(salesPanel);
            frame.revalidate();
            frame.repaint();
        });
        return salesButton;
    }

    private JButton createPurchasesButton() {
        JButton purchasesButton = new JButton("Purchases");
        purchasesButton.setPreferredSize(new Dimension(200, 100));
        purchasesButton.addActionListener(e -> {
            PurchasesPanel purchasesPanel = new PurchasesPanel(inventoryDAO, frame);
            frame.setContentPane(purchasesPanel);
            frame.revalidate();
            frame.repaint();
        });
        return purchasesButton;
    }
    private void showAllAttendancePanel() {
        JPanel allAttendancePanel = createAllAttendancePanel();
        frame.setContentPane(allAttendancePanel);
        frame.revalidate();
        frame.repaint();
    }

    private JPanel createAllAttendancePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(new JScrollPane(createAttendanceTable()), BorderLayout.CENTER);

        // Filter Panel
        panel.add(createFilterPanel(), BorderLayout.NORTH);

        // Back Button
        JButton backButton = new JButton("Back");
        backButton.addActionListener(e -> {
            frame.setContentPane(this);
            frame.revalidate();
            frame.repaint();
        });
        panel.add(backButton, BorderLayout.SOUTH);

        return panel;
    }



    private JPanel createFilterPanel() {
        JPanel filterPanel = new JPanel();

        filterPanel.add(new JLabel("Filter by Username:"));
        filterUsernameTextField = new JTextField(15);
        filterUsernameTextField.getDocument().addDocumentListener(new FilterDocumentListener());

        filterPanel.add(filterUsernameTextField);

        filterPanel.add(new JLabel("Filter by Date (yyyy-mm-dd):"));
        filterDateTextField = new JTextField(10);
        filterDateTextField.getDocument().addDocumentListener(new FilterDocumentListener());

        filterPanel.add(filterDateTextField);

        return filterPanel;
    }

    private class FilterDocumentListener implements DocumentListener {
        @Override
        public void insertUpdate(DocumentEvent e) {
            updateRowFilter();
        }

        @Override
        public void removeUpdate(DocumentEvent e) {
            updateRowFilter();
        }

        @Override
        public void changedUpdate(DocumentEvent e) {
            updateRowFilter();
        }

        private void updateRowFilter() {
            RowFilter<DefaultTableModel, Object> rf = null;
            try {
                List<RowFilter<Object, Object>> filters = new ArrayList<>();
                if (!filterUsernameTextField.getText().trim().isEmpty()) {
                    filters.add(RowFilter.regexFilter("(?i)" + filterUsernameTextField.getText(), 1));
                }
                if (!filterDateTextField.getText().trim().isEmpty()) {
                    filters.add(RowFilter.regexFilter("^" + filterDateTextField.getText(), 2));
                }
                rf = RowFilter.andFilter(filters);
            } catch (PatternSyntaxException pse) {
                JOptionPane.showMessageDialog(null, "Bad regex pattern", "Bad regex pattern", JOptionPane.ERROR_MESSAGE);
                return;
            }
            TableRowSorter<DefaultTableModel> sorter = (TableRowSorter<DefaultTableModel>) attendanceTable.getRowSorter();
            sorter.setRowFilter(rf);
        }
    }


    private JTable createAttendanceTable() {
        String[] columns = {"Attendance ID", "Username", "Date", "Check In", "Check Out"};
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // This makes the table cells non-editable
            }
        };
        attendanceTable = new JTable(model);
        attendanceTable.setAutoCreateRowSorter(true);

        try {
            List<AttendanceRecord> records = attendanceDAO.getAllAttendanceRecords();
            for (AttendanceRecord record : records) {
                model.addRow(new Object[]{
                        record.getAttendanceId(),
                        record.getUsername(), // Assume getUsername() exists in AttendanceRecord
                        record.getDate(),
                        record.getCheckInTime(),
                        record.getCheckOutTime()
                });
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Database error occurred while fetching attendance records.");
        }

        return attendanceTable;
    }
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame();
            AdminDashboard adminDashboard = new AdminDashboard(frame);
            frame.setContentPane(adminDashboard);
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }
}