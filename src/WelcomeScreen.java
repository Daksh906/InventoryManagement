import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class WelcomeScreen extends JFrame {
    public WelcomeScreen() {

        setSize(900, 500);
        setLocationRelativeTo(null); // Center the window
        setTitle("Cafeteria Inventory Management");
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        Font labelFont = new Font("Helvetica", Font.BOLD, 32);
        Font textLabelFont = new Font("Helvetica", Font.PLAIN, 18);
        Font buttonFont = new Font("Helvetica", Font.BOLD, 18);
        Color buttonColor = new Color(0, 102, 204);
        Color buttonHoverColor = buttonColor.brighter();


        JPanel contentPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();

        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        ImageIcon originalImageIcon = new ImageIcon("D:\\Photoshop\\RAJDHANI\\raw images\\RD0.png");
        Image resizedImage = originalImageIcon.getImage().getScaledInstance(300, 200, Image.SCALE_SMOOTH);
        ImageIcon resizedImageIcon = new ImageIcon(resizedImage);

        JLabel imageLabel = new JLabel(resizedImageIcon);
        gbc.insets = new Insets(10, 0, 0, 0); // top padding
        contentPanel.add(imageLabel, gbc);

        JLabel welcomeLabel = new JLabel("Welcome to RD Cafeteria");
        welcomeLabel.setForeground(Color.DARK_GRAY);
        welcomeLabel.setFont(labelFont);
        gbc.insets = new Insets(10, 0, 0, 0); // top padding
        contentPanel.add(welcomeLabel, gbc);

        JLabel textLabel = new JLabel("Cafeteria Inventory Management System");
        textLabel.setHorizontalAlignment(JLabel.CENTER); // Center alignment
        textLabel.setFont(textLabelFont);
        textLabel.setForeground(Color.BLACK); // Text color
        gbc.insets = new Insets(10, 0, 0, 0); // top padding
        contentPanel.add(textLabel, gbc);

        JButton continueButton = new JButton("Get Started");
        continueButton.setBackground(buttonColor);
        continueButton.setForeground(Color.WHITE);
        continueButton.setFont(buttonFont);
        continueButton.setFocusPainted(false); // Remove focus ring around the button
        continueButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                continueButton.setBackground(buttonHoverColor);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                continueButton.setBackground(buttonColor);
            }
        });
        continueButton.addActionListener(action -> {
            System.out.println("Get Started Button Clicked");

            LoginScreen loginScreen = new LoginScreen();
            loginScreen.setVisible(true);
            dispose();
        });
        gbc.insets = new Insets(10, 0, 0, 0); // top padding
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        buttonPanel.setOpaque(false);
        buttonPanel.add(continueButton);
        contentPanel.add(buttonPanel, gbc);

        contentPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.GRAY, 1, true),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)));


        add(contentPanel, BorderLayout.CENTER);

        pack();

        setSize(900, 500);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            WelcomeScreen welcomeScreen = new WelcomeScreen();
            welcomeScreen.setVisible(true);
        });
    }
}

