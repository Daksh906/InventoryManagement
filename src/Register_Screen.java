import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import javax.swing.*;
import javax.swing.JOptionPane;

import java.awt.*;


public class Register_Screen extends javax.swing.JFrame {

    private String fname, lname, uname, pword, phone, des;

    // UI components
    private JTextField Reg_FNameF;
    private JTextField Reg_LNameF;
    private JTextField Reg_UNameF;
    private JPasswordField Reg_PWordF;
    private JTextField Reg_PNumF;
    private JRadioButton Reg_AdminRB;
    private JRadioButton Reg_StaffRB;
    private JButton Reg_RegisterButt;
    private JButton Reg_BackButt;
    private JLabel Reg_SigPagL;
    private JLabel Reg_UNameL;
    private JLabel Reg_PWordL;
    private JLabel Reg_FNameL;
    private JLabel Reg_LNameL;
    private JLabel Reg_PNumL;
    private JLabel Reg_DesL;
    private JPanel jPanel1;
    private JPanel jPanel2;
    private JLabel Reg_Logo;
    private JTextField Reg_SpecialCodeF;
    private JLabel Reg_SpecialCodeL;
    private static final String ADMIN_REGISTRATION_CODE = "Admin456";
    private static final String STAFF_REGISTRATION_CODE = "Staff456";

    public Register_Screen() {
        initComponents();

        ButtonGroup bg = new ButtonGroup();
        bg.add(Reg_AdminRB);
        bg.add(Reg_StaffRB);
    }

    public void insertRegDetails(String specialCode) {
        // Retrieve user input
        fname = Reg_FNameF.getText();
        lname = Reg_LNameF.getText();
        uname = Reg_UNameF.getText();
        pword = new String(Reg_PWordF.getPassword());
        phone = Reg_PNumF.getText();
        des = Reg_AdminRB.isSelected() ? "Admin" : "Staff";

        // Validate the special code
        if (!validateSpecialCode(specialCode, des)) {
            return; // Exit if the special code is not valid
        }

        try {
            String hashedPassword = hashPassword(pword); // Hash the password

            Connection conn = DatabaseConnection.getConnection();
            String sql = "INSERT INTO reg (first_name, last_name, username, password, phone_number, designation) VALUES (?, ?, ?, ?, ?, ?)";
            PreparedStatement pst = conn.prepareStatement(sql);

            pst.setString(1, fname);
            pst.setString(2, lname);
            pst.setString(3, uname);
            pst.setString(4, hashedPassword); // Use the hashed password
            pst.setString(5, phone);
            pst.setString(6, des);

            int updatedRowCount = pst.executeUpdate();
            if (updatedRowCount > 0) {
                JOptionPane.showMessageDialog(this, "Registration successful.");
                new LoginScreen().setVisible(true);
                this.dispose();
            } else {
                JOptionPane.showMessageDialog(this, "Registration failed.");
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "A database error occurred.");
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            JOptionPane.showMessageDialog(this, "Error hashing the password.");
            e.printStackTrace();
        }
    }


    private boolean validateSpecialCode(String specialCode, String designation) {
        if ("Admin".equals(designation) && !ADMIN_REGISTRATION_CODE.equals(specialCode)) {
            JOptionPane.showMessageDialog(this, "Invalid admin registrationcode.");
            return false;
        } else if ("Staff".equals(designation) && !STAFF_REGISTRATION_CODE.equals(specialCode)) {
            JOptionPane.showMessageDialog(this, "Invalid staff registration code.");
            return false;
        }
        return true;
    }


    private static String hashPassword(String password) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        md.update(password.getBytes());
        byte[] digest = md.digest();
        return Base64.getEncoder().encodeToString(digest);
    }

    //Method to check if all information is entered
    public boolean checkFields() {

        fname = Reg_FNameF.getText();
        lname = Reg_LNameF.getText();
        uname = Reg_UNameF.getText();
        pword = Reg_PWordF.getText();
        phone = Reg_PNumF.getText();

        if (fname.trim().equals("") || lname.trim().equals("") || uname.trim().equals("") || pword.trim().equals("") || phone.trim().equals("")) {
            JOptionPane.showMessageDialog(this, "Enter all information");
            return false;
        }
        if (!Reg_AdminRB.isSelected() & !Reg_StaffRB.isSelected()) {
            JOptionPane.showMessageDialog(this, "Select a designation.");
            return false;
        } else {
            return true;
        }
    }

    //Method to check username
    // Modify the method signature to accept a String parameter
    public boolean checkUsername(String uname) {
        boolean exists = false;

        try {
            Connection conn = DatabaseConnection.getConnection();
            PreparedStatement pst = conn.prepareStatement("select * from reg where username = ?");
            pst.setString(1, uname);
            ResultSet rs = pst.executeQuery();

            if (rs.next()) {
                exists = true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return exists;
    }


    //Method to check if user with number exists
    //Method to check if user with a specific phone number exists
    public boolean checkNumEx(String phone) {
        boolean exists = false;

        try {
            Connection conn = DatabaseConnection.getConnection();
            PreparedStatement pst = conn.prepareStatement("select * from reg where phone_number = ?");
            pst.setString(1, phone);
            ResultSet rs = pst.executeQuery();

            if (rs.next()) {
                exists = true;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return exists;
    }


    //Method to check phone number
    public boolean checkNumber(String phone) {
        boolean valid = false;

        char[] num = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9'};
        int count = 0;
        if (phone.length() == 10) {
            for (int i = 0; i < phone.length(); i++) {
                if (Character.isDigit(phone.charAt(i))) {
                    count++;
                }
            }
            if (count == 10) {
                valid = true;
            }
        }
        return valid;
    }


    private void initComponents() {
        // Increase frame size
        setSize(800, 600); // Increased frame width and height
        setLocationRelativeTo(null);
        setTitle("Register Screen");
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        jPanel1 = new JPanel();
        jPanel1.setLayout(null); // Using null layout

        // Recalculate the horizontal center position for the image and components
        int frameWidth = 800; // Updated frame width


        // Component dimensions
        int labelWidth = 80;
        int fieldWidth = 200; // Slightly increased field width for better alignment
        int componentHeight = 30; // Slightly increased for better visibility
        int gap = 20; // Increased gap for better spacing
        int totalWidth = labelWidth + gap + fieldWidth;

        // Calculate the new center position for components based on the updated frame size
        int buttonWidth = 100; // Width of each button
        int radioButtonWidth = 80; // Width of each radio button
        int startY = 130; // Start position for the first component (y-coordinate)
        int shiftLeft = 15;
        int centerPosXButtons = ((frameWidth - totalWidth) / 2) - shiftLeft;
        int centerPosX = ((frameWidth - totalWidth) / 2) + 100;
        int imageWidth = (int)(200 * 1.2); // 240 pixels wide
        int imageHeight = (int)(75 * 1.2);
        // Load the image from the given file path and resize it
        ImageIcon originalIcon = new ImageIcon("D:\\Photoshop\\RAJDHANI\\raw images\\RD0.png");
        Image image = originalIcon.getImage();
        Image resizedImage = image.getScaledInstance(150, 100, Image.SCALE_SMOOTH); // Resize to match the LoginScreen
        ImageIcon imageIcon = new ImageIcon(resizedImage);

        // Create a JLabel with the resized ImageIcon
        Reg_Logo = new JLabel(imageIcon);
        Reg_Logo.setBounds(centerPosX, 20, 150, 100); // Set the bounds for the logo
        jPanel1.add(Reg_Logo);


        Reg_FNameL = new JLabel("First Name:");
        Reg_FNameL.setBounds(centerPosXButtons, startY, labelWidth, componentHeight);
        jPanel1.add(Reg_FNameL);

        Reg_FNameF = new JTextField();
        Reg_FNameF.setBounds(centerPosXButtons + labelWidth + gap, startY, fieldWidth, componentHeight);
        jPanel1.add(Reg_FNameF);

        Reg_LNameL = new JLabel("Last Name:");
        Reg_LNameL.setBounds(centerPosXButtons, startY + 40, labelWidth, componentHeight);
        jPanel1.add(Reg_LNameL);

        Reg_LNameF = new JTextField();
        Reg_LNameF.setBounds(centerPosXButtons + labelWidth + gap, startY + 40, fieldWidth, componentHeight);
        jPanel1.add(Reg_LNameF);
        Reg_UNameL = new JLabel("Username:");
        Reg_UNameL.setBounds(centerPosXButtons, startY + 80, labelWidth, componentHeight);
        jPanel1.add(Reg_UNameL);

        Reg_UNameF = new JTextField();
        Reg_UNameF.setBounds(centerPosXButtons + labelWidth + gap, startY + 80, fieldWidth, componentHeight);
        jPanel1.add(Reg_UNameF);

// Password
        Reg_PWordL = new JLabel("Password:");
        Reg_PWordL.setBounds(centerPosXButtons, startY + 120, labelWidth, componentHeight);
        jPanel1.add(Reg_PWordL);

        Reg_PWordF = new JPasswordField();
        Reg_PWordF.setBounds(centerPosXButtons + labelWidth + gap, startY + 120, fieldWidth, componentHeight);
        jPanel1.add(Reg_PWordF);

// Phone
        Reg_PNumL = new JLabel("Phone:");
        Reg_PNumL.setBounds(centerPosXButtons, startY + 160, labelWidth, componentHeight);
        jPanel1.add(Reg_PNumL);

        Reg_PNumF = new JTextField();
        Reg_PNumF.setBounds(centerPosXButtons + labelWidth + gap, startY + 160, fieldWidth, componentHeight);
        jPanel1.add(Reg_PNumF);

// Designation
        Reg_DesL = new JLabel("Designation:");
        Reg_DesL.setBounds(centerPosXButtons, startY + 200, labelWidth, componentHeight);
        jPanel1.add(Reg_DesL);

        Reg_AdminRB = new JRadioButton("Admin");
        Reg_AdminRB.setBounds(centerPosXButtons + labelWidth + gap, startY +200, radioButtonWidth, componentHeight);
        jPanel1.add(Reg_AdminRB);

        Reg_StaffRB = new JRadioButton("Staff");
        Reg_StaffRB.setBounds(centerPosXButtons + labelWidth + gap + radioButtonWidth, startY + 200, radioButtonWidth, componentHeight);
        jPanel1.add(Reg_StaffRB);

// Button group for radio buttons
        ButtonGroup bg = new ButtonGroup();
        bg.add(Reg_AdminRB);
        bg.add(Reg_StaffRB);

// Special Code
        Reg_SpecialCodeL = new JLabel("Special Code:");
        Reg_SpecialCodeL.setBounds(centerPosXButtons, startY + 240, labelWidth, componentHeight);
        jPanel1.add(Reg_SpecialCodeL);

        Reg_SpecialCodeF = new JTextField();
        Reg_SpecialCodeF.setBounds(centerPosXButtons + labelWidth + gap, startY + 240, fieldWidth, componentHeight);
        jPanel1.add(Reg_SpecialCodeF);

// Register Button
        Reg_RegisterButt = new JButton("Register");
        Reg_RegisterButt.setBounds(centerPosXButtons + 70, startY + 280, buttonWidth, componentHeight);
        Reg_RegisterButt.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                Reg_RegisterButtActionPerformed(evt);
            }
        });
        jPanel1.add(Reg_RegisterButt);

// Back Button
        Reg_BackButt = new JButton("Back");
        Reg_BackButt.setBounds(centerPosXButtons + buttonWidth + gap + 70, startY + 280, buttonWidth, componentHeight);
        Reg_BackButt.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                Reg_BackButtActionPerformed(evt);
            }
        });
        jPanel1.add(Reg_BackButt);

// Add the panel to the frame
        getContentPane().add(jPanel1);
        setLocationRelativeTo(null); // Center the window on screen
    }







        private void Reg_RegisterButtActionPerformed(java.awt.event.ActionEvent evt) {
        // Retrieve user input from the form
        fname = Reg_FNameF.getText();
        lname = Reg_LNameF.getText();
        uname = Reg_UNameF.getText();
        pword = new String(Reg_PWordF.getPassword());
        phone = Reg_PNumF.getText();
        String specialCode = Reg_SpecialCodeF.getText(); // Get the special code from the text field

        // Check if all fields are filled in
        if (fname.isEmpty() || lname.isEmpty() || uname.isEmpty() || pword.isEmpty() || phone.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter all information.");
            return;
        }

        // Check if the designation is selected
        if (!Reg_AdminRB.isSelected() && !Reg_StaffRB.isSelected()) {
            JOptionPane.showMessageDialog(this, "Please select a designation.");
            return;
        }

        // Determine the designation and validate the special code
        des = Reg_AdminRB.isSelected() ? "Admin" : "Staff";
        if (!validateSpecialCode(specialCode, des)) {
            // Error message is shown inside validateSpecialCode method
            return;
        }

        // Check if username already exists
        if (checkUsername(uname)) { // Pass the username as an argument
            JOptionPane.showMessageDialog(this, "Username already exists.");
            return;
        }

        // Check if phone number is valid and does not already exist
        if (!checkNumber(phone) || checkNumEx(phone)) {
            JOptionPane.showMessageDialog(this, "Invalid or existing phone number.");
            return;
        }

        // If everything is valid, proceed to register the user
        insertRegDetails(specialCode); // Passing the special code to the method
    }






    private void Reg_UNameFFocusLost(java.awt.event.FocusEvent evt) {
        String enteredUsername = Reg_UNameF.getText();
        if (checkUsername(enteredUsername)) {
            JOptionPane.showMessageDialog(this, "Username unavailable.");
        }
    }



    private void Reg_BackButtActionPerformed(java.awt.event.ActionEvent evt) {
        LoginScreen lp = new LoginScreen();
        lp.setVisible(true);
        this.dispose();
    }

    private void Reg_PNumFFocusLost(java.awt.event.FocusEvent evt) {
        String enteredPhone = Reg_PNumF.getText();
        if (!enteredPhone.trim().equals("")) {
            if (!checkNumber(enteredPhone)) {
                JOptionPane.showMessageDialog(this, "Phone Number Invalid");
            } else if (checkNumEx(enteredPhone)) {
                JOptionPane.showMessageDialog(this, "This Phone Number is already registered with an account.");
            }
        }
    }


    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new Register_Screen().setVisible(true);
            }
        });
    }
}