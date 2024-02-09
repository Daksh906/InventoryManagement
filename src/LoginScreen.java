import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.border.EmptyBorder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Arrays;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class LoginScreen extends JFrame {
    private JTextField usernameTextField;
    private JPasswordField passwordField;
    private JButton loginButton;

    public LoginScreen() {
        setSize(800, 500);
        setLocationRelativeTo(null);
        setTitle("Login Screen");
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        add(getHeader(), BorderLayout.NORTH);
        add(getMainPanel(), BorderLayout.CENTER);

        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowOpened(java.awt.event.WindowEvent evt) {
                usernameTextField.requestFocus();
            }
        });

        // Make the frame visible
        setVisible(true);
    }

    private JPanel getHeader() {
        JPanel headerPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        headerPanel.setBorder(new EmptyBorder(20, 0, 20, 0));

        // Load the image from the given file path
        ImageIcon originalIcon = new ImageIcon("D:\\Photoshop\\RAJDHANI\\raw images\\RD0.png");
        // Resize the image icon
        Image image = originalIcon.getImage();
        Image resizedImage = image.getScaledInstance(150, 100, Image.SCALE_SMOOTH); // Resize to 100x100px or as needed
        ImageIcon imageIcon = new ImageIcon(resizedImage);

        JLabel imageLabel = new JLabel(imageIcon);

        // Add the image label to the header panel
        headerPanel.add(imageLabel);

        return headerPanel;
    }



    private JPanel getMainPanel() {
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));

        mainPanel.add(getUsernamePanel());
        mainPanel.add(getPasswordPanel());
        mainPanel.add(getForgotPasswordPanel()); // This line has been moved up
        mainPanel.add(getLoginButtonPanel());
        mainPanel.add(getRegisterButtonPanel()); // This will now add a styled label instead of a button
        return mainPanel;
    }

    private JPanel getUsernamePanel() {
        JPanel usernamePanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        usernamePanel.setBorder(new EmptyBorder(10, 0, 10, 0));

        JLabel usernameLabel = new JLabel("Username:");
        usernameTextField = new JTextField(16);

        usernamePanel.add(usernameLabel);
        usernamePanel.add(usernameTextField);

        return usernamePanel;
    }

    private JPanel getPasswordPanel() {
        JPanel passwordPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        passwordPanel.setBorder(new EmptyBorder(10, 0, 10, 0));

        JLabel passwordLabel = new JLabel("Password:");
        passwordField = new JPasswordField(16);

        passwordPanel.add(passwordLabel);
        passwordPanel.add(passwordField);

        return passwordPanel;
    }
    private JPanel getForgotPasswordPanel() {
        JPanel forgotPasswordPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JLabel forgotPasswordLabel = new JLabel("Forgot Password?");
        forgotPasswordLabel.setForeground(Color.BLUE);
        forgotPasswordLabel.setCursor(new Cursor(Cursor.HAND_CURSOR));
        forgotPasswordLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                openForgotPasswordScreen();
            }
        });
        forgotPasswordPanel.add(forgotPasswordLabel);
        return forgotPasswordPanel;
    }
    private void openForgotPasswordScreen() {
        ForgotPassword forgotPassword = new ForgotPassword();
        forgotPassword.setVisible(true);
        this.dispose(); // Optionally, close the login window
    }

    private JPanel getLoginButtonPanel() {
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        buttonPanel.setBorder(new EmptyBorder(20, 0, 30, 0));

        loginButton = new GradientButton("Login");
        loginButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                performLogin();
            }
        });

        buttonPanel.add(loginButton);
        return buttonPanel;
    }

    private JPanel getRegisterButtonPanel() {
        JPanel registerButtonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        registerButtonPanel.setBorder(new EmptyBorder(0, 0, 25, 5));

        JLabel signUpLabel = new JLabel("Sign Up"); // Changed from JButton to JLabel
        signUpLabel.setForeground(Color.BLUE);
        signUpLabel.setCursor(new Cursor(Cursor.HAND_CURSOR));
        signUpLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                openRegistrationScreen();
            }
        });

        registerButtonPanel.add(signUpLabel); // Add label to the panel
        return registerButtonPanel;
    }
    private void openRegistrationScreen() {
        Register_Screen registerScreen = new Register_Screen();
        registerScreen.setVisible(true);
        this.dispose(); // Close the login screen
    }
    public class LoginResult {
        private final String designation;
        private final int staffId;

        public LoginResult(String designation, int staffId) {
            this.designation = designation;
            this.staffId = staffId;
        }

        public String getDesignation() {
            return designation;
        }

        public int getStaffId() {
            return staffId;
        }
    }

    // The authenticate method in the context where it is defined (e.g., within a class)
    public LoginResult authenticate(String username, char[] password) {
        try {
            Connection conn = DatabaseConnection.getConnection();
            String sql = "SELECT id, password, designation FROM reg WHERE username = ?";
            PreparedStatement pst = conn.prepareStatement(sql);

            pst.setString(1, username);
            ResultSet rs = pst.executeQuery();

            if (rs.next()) {
                String storedPassword = rs.getString("password");
                String hashedEnteredPassword = hashPassword(new String(password));

                if (hashedEnteredPassword.equals(storedPassword)) {
                    int staffId = rs.getInt("id"); // Retrieve staff ID from the database
                    String designation = rs.getString("designation");
                    return new LoginResult(designation, staffId); // Return both designation and staff ID
                }
            }
        } catch (SQLException | NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        Arrays.fill(password, '0');
        return null; // Return null if authentication fails
    }

    // The performLogin method
    private void performLogin() {
        String username = usernameTextField.getText();
        char[] password = passwordField.getPassword();

        // Call the authenticate method to check the credentials
        LoginResult loginResult = authenticate(username, password);

        if (loginResult != null) {
            // Redirect based on the user's designation
            if ("Admin".equals(loginResult.getDesignation())) {
                JFrame mainFrame = new JFrame();
                AdminDashboard adminDashboard = new AdminDashboard(mainFrame);
                mainFrame.setContentPane(adminDashboard);
                mainFrame.setLocationRelativeTo(null); // Centers the window
                mainFrame.setVisible(true);
            } else if ("Staff".equals(loginResult.getDesignation())) {
                // Redirect to the staff dashboard
                StaffDashboard staffDashboard = new StaffDashboard(loginResult.getStaffId());
                staffDashboard.setLocationRelativeTo(null); // Centers the window
                staffDashboard.setVisible(true);
            }
            dispose(); // Close the login window
        } else {
            // Authentication failed
            onAuthenticationFailure();
        }
    }


    private static String hashPassword(String password) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        md.update(password.getBytes());
        byte[] digest = md.digest();
        return Base64.getEncoder().encodeToString(digest);
    }







    private void onAuthenticationFailure() {
        // Clear the password field
        passwordField.setText("");
        // Set the background color to indicate error
        usernameTextField.setBackground(Color.PINK);
        passwordField.setBackground(Color.PINK);

        // Reset the background color after a delay
        Timer resetColorTimer = new Timer(1000, e -> {
            usernameTextField.setBackground(Color.WHITE);
            passwordField.setBackground(Color.WHITE);
        });
        resetColorTimer.setRepeats(false);
        resetColorTimer.start();

        // Show error message
        JOptionPane.showMessageDialog(this, "Invalid username or password", "Login Error", JOptionPane.ERROR_MESSAGE);
    }

    private class GradientButton extends JButton {
        private Color defaultBackgroundColor = new Color(100, 149, 237); // Cornflower blue
        private Color hoverBackgroundColor = new Color(30, 144, 255); // Dodger blue
        private Color pressedBackgroundColor = new Color(0, 82, 164); // Darker blue when pressed

        public GradientButton(String text) {
            super(text);
            setBackground(defaultBackgroundColor); // Set the default background color
            setForeground(Color.WHITE);
            setFocusPainted(false);
            setContentAreaFilled(false);
            setBorderPainted(false);
            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    setBackground(hoverBackgroundColor);
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    setBackground(defaultBackgroundColor); // Reset to the default color
                }

                @Override
                public void mousePressed(MouseEvent e) {
                    setBackground(pressedBackgroundColor);
                }

                @Override
                public void mouseReleased(MouseEvent e) {
                    setBackground(getModel().isRollover() ? hoverBackgroundColor : defaultBackgroundColor);
                }

            });
        }

        @Override
        protected void paintComponent(Graphics g) {
            g.setColor(getBackground());
            g.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
            super.paintComponent(g);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                new LoginScreen();
            }
        });
    }
}