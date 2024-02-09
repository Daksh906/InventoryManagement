import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.SQLException;
import java.util.List;

public class SalesPanel extends JPanel {
    private SalesDAO salesDAO;
    private JTable salesTable;
    private DefaultTableModel salesModel;
    private InventoryDAO inventoryDAO;
    private JFrame frame;

    public SalesPanel(SalesDAO salesDAO, InventoryDAO inventoryDAO, JFrame frame) {
        this.salesDAO = salesDAO;
        this.inventoryDAO = inventoryDAO;
        this.frame = frame;
        setLayout(new BorderLayout());
        initializeSalesTable();
        add(createButtonPanel(), BorderLayout.SOUTH);
    }

    private void initializeSalesTable() {
        String[] columnNames = {
                "Product Code", "Product Name", "Stock Sold", "Total Stock", "Selling Price" , "Sales Revenue"
        };

        salesModel = new DefaultTableModel(columnNames, 0);
        salesTable = new JTable(salesModel);
        add(new JScrollPane(salesTable), BorderLayout.CENTER);
        refreshSalesData();
    }

    public void refreshSalesData() {
        salesModel.setRowCount(0);
        try {
            List<Sale> sales = salesDAO.selectAllSales();
            for (Sale sale : sales) {
                Object[] row = new Object[]{
                        sale.getProductID(),
                        sale.getProductName(),
                        sale.getStockSold(),
                        sale.getTotalStock(),
                        sale.getSellingPrice(),
                        sale.calculateSalesRevenue()
                };
                salesModel.addRow(row);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error refreshing sales data: " + e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }




    private JButton createAddButton() {
        JButton addButton = new JButton("Add Sale");
        addButton.addActionListener(this::addSale);
        return addButton;
    }

    private JButton createEditButton() {
        JButton editButton = new JButton("Edit Sale");
        editButton.addActionListener(this::editSale);
        return editButton;
    }

    private JButton createDeleteButton() {
        JButton deleteButton = new JButton("Delete Sale");
        deleteButton.addActionListener(this::deleteSale);
        return deleteButton;
    }

    private JPanel createButtonPanel() {
        JPanel buttonPanel = new JPanel();
        buttonPanel.add(createAddButton());
        buttonPanel.add(createEditButton());
        buttonPanel.add(createDeleteButton());
        buttonPanel.add(createBackButton());
        buttonPanel.add(createRefreshButton());// Add the back button to the panel
        return buttonPanel;
    }

    private JButton createRefreshButton() {
        JButton refreshButton = new JButton("Refresh Stock");
        refreshButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                refreshTotalStockColumn();
            }
        });
        return refreshButton;
    }

    private void refreshTotalStockColumn() {
        for (int i = 0; i < salesModel.getRowCount(); i++) {
            int productID = (Integer) salesModel.getValueAt(i, 0); // Assuming first column is ProductID
            try {
                Product product = inventoryDAO.selectProduct(productID);
                if (product != null) {
                    // Assuming the TotalStock is in a specific column in your salesModel
                    salesModel.setValueAt(product.getQuantity(), i,3);
                }
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Error fetching product data: " + ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }


    private void addSale(ActionEvent e) {
        // Retrieve the list of products for selection
        List<Product> productList;
        try {
            productList = inventoryDAO.selectAllProducts();
            System.out.println("Products retrieved successfully.");
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error retrieving products: " + ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        Product[] productsArray = productList.toArray(new Product[0]);
        JComboBox<Product> productComboBox = new JComboBox<>(productsArray);

        int result = JOptionPane.showConfirmDialog(this, productComboBox, "Select Product", JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);
        if (result == JOptionPane.OK_OPTION) {
            Product selectedProduct = (Product) productComboBox.getSelectedItem();
            if (selectedProduct != null) {
                try {
                    int quantitySold = Integer.parseInt(JOptionPane.showInputDialog(this, "Enter quantity sold:"));
                    double sellingPrice = Double.parseDouble(JOptionPane.showInputDialog(this, "Enter selling price per unit:"));
                    int totalStock = selectedProduct.getQuantity(); // Assuming getQuantity() gives the current total stock for the product

                    // Now include totalStock in the constructor
                    Sale newSale = new Sale(selectedProduct.getProductID(), selectedProduct.getName(), quantitySold, totalStock, sellingPrice);
                    System.out.println("New sale created: " + newSale);

                    salesDAO.insertSale(newSale);
                    System.out.println("Sale inserted into database.");

                    refreshSalesData();
                    System.out.println("Sales data refreshed.");

                    JOptionPane.showMessageDialog(this, "Sale added successfully.", "Sale Added", JOptionPane.INFORMATION_MESSAGE);
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(this, "Please enter valid numbers for quantity and price.", "Invalid Input", JOptionPane.ERROR_MESSAGE);
                } catch (SQLException ex) {
                    JOptionPane.showMessageDialog(this, "Error adding sale: " + ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        }
    }






    private void editSale(ActionEvent e) {
        int selectedRow = salesTable.getSelectedRow();
        if (selectedRow >= 0) {
            int productID = (Integer) salesModel.getValueAt(selectedRow, 0);

            try {
                Sale existingSale = salesDAO.selectSale(productID);
                int originalQuantitySold = existingSale.getStockSold(); // Store the original quantity sold

                JTextField quantitySoldField = new JTextField(String.valueOf(existingSale.getStockSold()));
                JTextField sellingPriceField = new JTextField(String.format("%.2f", existingSale.getSellingPrice()));

                Object[] message = {
                        "Quantity Sold:", quantitySoldField,
                        "Selling Price:", sellingPriceField
                };

                int result = JOptionPane.showConfirmDialog(this, message, "Edit Sale", JOptionPane.OK_CANCEL_OPTION);
                if (result == JOptionPane.OK_OPTION) {
                    int newQuantitySold = Integer.parseInt(quantitySoldField.getText());
                    double newSellingPrice = Double.parseDouble(sellingPriceField.getText());

                    existingSale.setStockSold(newQuantitySold);
                    existingSale.setSellingPrice(newSellingPrice);

                    salesDAO.updateSale(existingSale, originalQuantitySold);

                    refreshSalesData();
                    JOptionPane.showMessageDialog(this, "Sale updated successfully.", "Sale Updated", JOptionPane.INFORMATION_MESSAGE);
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Invalid number format for quantity or price.", "Invalid Input", JOptionPane.ERROR_MESSAGE);
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Error updating sale: " + ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
            }
        } else {
            JOptionPane.showMessageDialog(this, "Please select a sale to edit.", "No Sale Selected", JOptionPane.ERROR_MESSAGE);
        }
    }


    private void deleteSale(ActionEvent e) {

        int selectedRow = salesTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a sale to delete.", "No Sale Selected", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Confirm deletion
        int response = JOptionPane.showConfirmDialog(this, "Are you sure you want to delete the selected sale?", "Confirm Deletion", JOptionPane.YES_NO_OPTION);
        if (response != JOptionPane.YES_OPTION) {
            return;
        }

        // Proceed with deletion
        try {
            // Assuming the Sale ID is in the first column of the table model.
            int saleId = (int) salesTable.getValueAt(selectedRow, 0);
            salesDAO.deleteSale(saleId);
            refreshSalesData();
            JOptionPane.showMessageDialog(this, "Sale deleted successfully.", "Sale Deleted", JOptionPane.INFORMATION_MESSAGE);
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error deleting sale: " + ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        }


    }
    public void confirmSale(Sale sale) {
        try {
            salesDAO.insertSale(sale);
            // Refresh UI, tables, etc.
            JOptionPane.showMessageDialog(this, "Sale recorded successfully.");
        } catch (SQLException e) {
            // Handle errors
            JOptionPane.showMessageDialog(this, "Error recording sale: " + e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }


    private JButton createBackButton() {
        JButton backButton = new JButton("Back");
        backButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                goBack();
            }
        });
        return backButton;
    }

    // Method to switch back to the AdminDashboard panel
    private void goBack() {
        frame.setContentPane(new AdminDashboard(frame)); // Pass the frame to the constructor of AdminDashboard
        frame.revalidate();
        frame.repaint();
    }
}