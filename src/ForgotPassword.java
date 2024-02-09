import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class ForgotPassword extends JFrame {
    private JTextField firstNameTextField, lastNameTextField, phoneNumberTextField;
    private JPasswordField newPasswordField;
    private JButton resetPasswordButton;
    private JLabel logoLabel;
    private JButton backButton;
    public ForgotPassword() {
        setTitle("Forgot Password");
        setSize(700, 500); // Match the size with LoginScreen
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(null); // Use null layout for positioning control
        setLocationRelativeTo(null); // Center the window

        // Logo
        ImageIcon originalIcon = new ImageIcon("D:\\Photoshop\\RAJDHANI\\raw images\\RD0.png");
        Image image = originalIcon.getImage();
        Image resizedImage = image.getScaledInstance(150, 100, Image.SCALE_SMOOTH);
        ImageIcon imageIcon = new ImageIcon(resizedImage);
        logoLabel = new JLabel(imageIcon);
        logoLabel.setBounds((getWidth() - 150) / 2, 10, 150, 100); // Center the logo
        add(logoLabel);

        int labelWidth = 100;
        int fieldWidth = 160;
        int fieldHeight = 25;
        int verticalGap = 30;
        int horizontalGap = 20;
        int topMargin = 120; // Adjusted to make space for logo

        JLabel firstNameLabel = new JLabel("First Name:");
        firstNameLabel.setBounds((getWidth() - labelWidth - fieldWidth - horizontalGap) / 2, topMargin, labelWidth, fieldHeight);
        add(firstNameLabel);

        firstNameTextField = new JTextField();
        firstNameTextField.setBounds(firstNameLabel.getX() + firstNameLabel.getWidth() + horizontalGap, topMargin, fieldWidth, fieldHeight);
        add(firstNameTextField);

        JLabel lastNameLabel = new JLabel("Last Name:");
        lastNameLabel.setBounds(firstNameLabel.getX(), firstNameLabel.getY() + firstNameLabel.getHeight() + verticalGap, labelWidth, fieldHeight);
        add(lastNameLabel);

        lastNameTextField = new JTextField();
        lastNameTextField.setBounds(firstNameTextField.getX(), lastNameLabel.getY(), fieldWidth, fieldHeight);
        add(lastNameTextField);

        JLabel phoneNumberLabel = new JLabel("Phone Number:");
        phoneNumberLabel.setBounds(firstNameLabel.getX(), lastNameLabel.getY() + lastNameLabel.getHeight() + verticalGap, labelWidth, fieldHeight);
        add(phoneNumberLabel);

        phoneNumberTextField = new JTextField();
        phoneNumberTextField.setBounds(firstNameTextField.getX(), phoneNumberLabel.getY(), fieldWidth, fieldHeight);
        add(phoneNumberTextField);

        JLabel newPasswordLabel = new JLabel("New Password:");
        newPasswordLabel.setBounds(firstNameLabel.getX(), phoneNumberLabel.getY() + phoneNumberLabel.getHeight() + verticalGap, labelWidth, fieldHeight);
        add(newPasswordLabel);

        newPasswordField = new JPasswordField();
        newPasswordField.setBounds(firstNameTextField.getX(), newPasswordLabel.getY(), fieldWidth, fieldHeight);
        add(newPasswordField);

        int buttonWidth = 100; // Reduced button width
        int buttonHeight = 25; // Standard button height
        int buttonY = 350;

        resetPasswordButton = new JButton("Reset");
        resetPasswordButton.setBounds((getWidth() / 2) - (buttonWidth + 10), buttonY, buttonWidth, buttonHeight); // Positioning Reset Password button
        resetPasswordButton.addActionListener(this::resetPassword);
        add(resetPasswordButton);

        // Position the Back button
        backButton = new JButton("Back");
        backButton.setBounds((getWidth() / 2) + 10, buttonY, buttonWidth, buttonHeight); // Positioning Back button
        backButton.addActionListener(e -> openLoginScreen());
        add(backButton);


        setVisible(true);

    }

    private void openLoginScreen() {
        LoginScreen loginScreen = new LoginScreen(); // Assume LoginScreen is your login form
        loginScreen.setVisible(true);
        dispose(); // Close the ForgotPassword screen
    }
    private void resetPassword(ActionEvent event) { // Renamed 'e' to 'event' to avoid conflict
        String firstName = firstNameTextField.getText();
        String lastName = lastNameTextField.getText();
        String phoneNumber = phoneNumberTextField.getText();
        String newPassword = new String(newPasswordField.getPassword());

        try {
            Connection conn = DatabaseConnection.getConnection();
            String sql = "UPDATE reg SET password = ? WHERE first_name = ? AND last_name = ? AND phone_number = ?";
            PreparedStatement pst = conn.prepareStatement(sql);

            pst.setString(1, hashPassword(newPassword));
            pst.setString(2, firstName);
            pst.setString(3, lastName);
            pst.setString(4, phoneNumber);

            int rowsAffected = pst.executeUpdate();
            if (rowsAffected > 0) {
                JOptionPane.showMessageDialog(this, "Password reset successfully.");
            } else {
                JOptionPane.showMessageDialog(this, "No user found with provided details.");
            }
        } catch (SQLException ex) { // Changed 'e' to 'ex' to avoid conflict
            JOptionPane.showMessageDialog(this, "A database error occurred.");
            ex.printStackTrace();
        } catch (NoSuchAlgorithmException ex) { // Changed 'e' to 'ex' to avoid conflict
            JOptionPane.showMessageDialog(this, "Could not hash the password.");
            ex.printStackTrace();
        }
    }



    private static String hashPassword(String password) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        md.update(password.getBytes());
        byte[] digest = md.digest();
        return Base64.getEncoder().encodeToString(digest);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                new ForgotPassword();
            }
        });
    }
}
