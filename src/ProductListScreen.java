import javax.swing.*;
import java.awt.*;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Date;
public class ProductListScreen extends JFrame {
    private InventoryDAO inventoryDAO;
    private List<Product> products;
    private POSBillScreen posBillScreen; // Reference to POSBillScreen
    private JPanel gridPanel;
    private JLabel dateTimeLabel;
    private StaffDashboard staffDashboard;

    public ProductListScreen(ArrayList<Product> products, POSBillScreen posBillScreen, StaffDashboard staffDashboard) {
        this.posBillScreen = posBillScreen; // Existing code
        this.inventoryDAO = new InventoryDAO(); // Existing code
        this.products = products; // Existing code
        this.staffDashboard = staffDashboard; // Store the StaffDashboard reference

        initializeComponents();
    }

    private void initializeComponents() {
        setTitle("Product List");
        setSize(800, 600);
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());

        JPanel topPanel = createTopPanel();
        add(topPanel, BorderLayout.NORTH);

        gridPanel = createGridPanel();
        JScrollPane scrollPane = new JScrollPane(gridPanel);
        add(scrollPane, BorderLayout.CENTER);

        // Create a panel at the bottom for the back and proceed buttons
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        bottomPanel.add(createBackButton()); // Add the back button
        bottomPanel.add(createProceedToBillButton()); // Add the proceed button
        add(bottomPanel, BorderLayout.SOUTH); // Add the bottom panel to the SOUTH of the BorderLayout

        setLocationRelativeTo(null);
    }


    private JPanel createTopPanel() {
        JPanel topPanel = new JPanel(new BorderLayout());

        dateTimeLabel = new JLabel();
        updateDateTime();
        topPanel.add(dateTimeLabel, BorderLayout.WEST);

        JTextField searchField = new JTextField(20);
        JButton searchButton = new JButton("Search");
        searchButton.addActionListener(e -> filterProducts(searchField.getText()));

        JPanel searchPanel = new JPanel();
        searchPanel.add(searchField);
        searchPanel.add(searchButton);
        topPanel.add(searchPanel, BorderLayout.CENTER);

        return topPanel;
    }

    private JPanel createGridPanel() {
        JPanel gridPanel = new JPanel();
        gridPanel.setLayout(new GridLayout(0, 3, 10, 10));

        for (Product product : products) {
            gridPanel.add(createProductButton(product));
        }

        return gridPanel;
    }

    private void filterProducts(String searchText) {
        ArrayList<Product> filteredProducts = new ArrayList<>();
        for (Product product : products) {
            if (product.getName().toLowerCase().contains(searchText.toLowerCase())) {
                filteredProducts.add(product);
            }
        }

        gridPanel.removeAll();
        for (Product product : filteredProducts) {
            gridPanel.add(createProductButton(product));
        }

        gridPanel.revalidate();
        gridPanel.repaint();
    }

    private JButton createProductButton(Product product) {
        JButton button = new JButton(product.getName());
        ImageIcon icon = new ImageIcon(product.getImagePath());
        if (icon.getImageLoadStatus() == MediaTracker.COMPLETE) {
            Image image = icon.getImage().getScaledInstance(100, 100, Image.SCALE_SMOOTH);
            icon = new ImageIcon(image);
            button.setIcon(icon);
        } else {
            System.out.println("Failed to load image: " + product.getImagePath());
        }
        button.addActionListener(e -> {
            if (posBillScreen != null) {
                showProductDetails(product);
            }
        });
        button.setPreferredSize(new Dimension(200, 120));
        return button;
    }

    private JButton createBackButton() {
        JButton backButton = new JButton("Back to Dashboard");
        backButton.setPreferredSize(new Dimension(150, 50)); // Set the preferred size to match other buttons
        backButton.addActionListener(e -> {
            this.dispose(); // Close the current window
            staffDashboard.setVisible(true); // Reopen the StaffDashboard
        });
        return backButton;
    }

    private void showProductDetails(Product product) {
        JPanel detailsPanel = createProductDetailsPanel(product);
        JSpinner quantitySpinner = (JSpinner) detailsPanel.getComponent(7); // Ensure correct index

        int response = JOptionPane.showConfirmDialog(this, detailsPanel, "Product Details",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (response == JOptionPane.OK_OPTION) {
            int selectedQuantity = (Integer) quantitySpinner.getValue();

            if (selectedQuantity > product.getQuantity()) {
                JOptionPane.showMessageDialog(this,
                        "Error: Selected quantity exceeds available stock. Please select a lower quantity.",
                        "Quantity Error", JOptionPane.ERROR_MESSAGE);
                // Reopen the product details dialog for correction
                showProductDetails(product); // Recursive call
                return;
            }

            // Proceed to update the quantity in POSBillScreen and database
            posBillScreen.addItemToOrder(product, selectedQuantity);
            updateProductQuantityInDatabase(product, product.getQuantity() - selectedQuantity);
        }
    }




    private void updateProductQuantityInDatabase(Product product, int newQuantity) {
        try {
            product.setQuantity(newQuantity);
            inventoryDAO.updateProduct(product); // This should now work without NullPointerException
            refreshProductList();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error updating product quantity in database.",
                    "Database Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    private JPanel createProductDetailsPanel(Product product) {
        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(0, 2)); // Grid layout with 2 columns

        // Add product details
        panel.add(new JLabel("Name:"));
        panel.add(new JLabel(product.getName()));

        panel.add(new JLabel("Price:"));
        panel.add(new JLabel(String.format("$%.2f", product.getPrice())));

        panel.add(new JLabel("Available Quantity:"));
        panel.add(new JLabel(String.valueOf(product.getQuantity())));

        // Input for desired quantity
        panel.add(new JLabel("Order Quantity:"));
        SpinnerNumberModel model = new SpinnerNumberModel(1, 1, Integer.MAX_VALUE, 1);
        JSpinner quantitySpinner = new JSpinner(model);
        panel.add(quantitySpinner);

        return panel;
    }

    private JButton createProceedToBillButton() {
        JButton proceedToBillButton = new JButton("Proceed to Bill");
        proceedToBillButton.setPreferredSize(new Dimension(150, 50)); // Set the preferred size to match the back button
            proceedToBillButton.addActionListener(e -> {
                if (posBillScreen != null) {
                    posBillScreen.setVisible(true);
                }
            });
        return proceedToBillButton;
    }

    private void updateDateTime() {
        String pattern = "EEE, d MMM yyyy HH:mm:ss";
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);
        Timer timer = new Timer(1000, e -> dateTimeLabel.setText(simpleDateFormat.format(new Date())));
        timer.start();
    }

    public void refreshProductList() {
        products = fetchProductsFromDatabase();
        gridPanel.removeAll();
        for (Product product : products) {
            gridPanel.add(createProductButton(product));
        }
        gridPanel.revalidate();
        gridPanel.repaint();
    }

    private List<Product> fetchProductsFromDatabase() {
        try {
            return inventoryDAO.selectAllProducts();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error fetching products from database.", "Database Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
            return new ArrayList<>(); // Return an empty list if there's an error
        }
    }

}