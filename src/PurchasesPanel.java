import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.SQLException;
import java.util.List;

public class PurchasesPanel extends JPanel {
    private InventoryDAO inventoryDAO;
    private JTable purchasesTable;
    private DefaultTableModel purchasesModel;
    private JFrame frame; // Reference to the main application window

    public PurchasesPanel(InventoryDAO inventoryDAO, JFrame frame) {
        this.inventoryDAO = inventoryDAO;
        this.frame = frame; // Store the reference to the main application window
        setLayout(new BorderLayout());
        initializePurchasesTable();
        add(createButtonPanel(), BorderLayout.SOUTH);

    }

    private void initializePurchasesTable() {
        String[] columnNames = {
                "Product Code", "Product Name", "Inventory Stock",
                "Purchased Stock", "Total Stock", "Cost Price", "Amount"
        };

        purchasesModel = new DefaultTableModel(columnNames, 0);
        purchasesTable = new JTable(purchasesModel);
        add(new JScrollPane(purchasesTable), BorderLayout.CENTER);
        refreshPurchasesData();
    }

    public void refreshPurchasesData() {
        purchasesModel.setRowCount(0); // Clear the table
        try {
            List<Purchase> purchases = inventoryDAO.selectAllPurchases();
            for (Purchase purchase : purchases) {
                Object[] row = new Object[]{
                        purchase.getProductID(),
                        purchase.getProductName(),
                        purchase.getInventoryStock(),
                        purchase.getPurchasedStock(),
                        purchase.getTotalStock(),
                        purchase.getCostPrice(),
                        purchase.getAmount()
                };
                purchasesModel.addRow(row); // Add row to the table model
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error refreshing purchase data: " + e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }
    private JPanel createButtonPanel() {
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        buttonPanel.add(createAddPurchaseButton());
        buttonPanel.add(createEditPurchaseButton());
        buttonPanel.add(createDeletePurchaseButton());
        buttonPanel.add(createBackButton());
        buttonPanel.add(createRefreshButton());
        return buttonPanel;
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
        for (int i = 0; i < purchasesModel.getRowCount(); i++) {
            int productID = (Integer) purchasesModel.getValueAt(i, 0); // Assuming first column is ProductID
            try {
                Product product = inventoryDAO.selectProduct(productID);
                if (product != null) {
                    // Assuming the fifth column is TotalStock
                    purchasesModel.setValueAt(product.getQuantity(), i, 4); // Update the TotalStock column
                }
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Error fetching product data: " + ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void goBack() {
        frame.setContentPane(new AdminDashboard(frame)); // Pass the frame to the constructor of AdminDashboard
        frame.revalidate();
        frame.repaint();
    }
    private JButton createAddPurchaseButton() {
        JButton addPurchaseButton = new JButton("Add Purchase");
        addPurchaseButton.addActionListener(this::addPurchase);
        return addPurchaseButton;
    }

    private JButton createEditPurchaseButton() {
        JButton editPurchaseButton = new JButton("Edit Purchase");
        editPurchaseButton.addActionListener(this::editPurchase);
        return editPurchaseButton;
    }

    private JButton createDeletePurchaseButton() {
        JButton deletePurchaseButton = new JButton("Delete Purchase");
        deletePurchaseButton.addActionListener(this::deletePurchase);
        return deletePurchaseButton;
    }

    private void addPurchase(ActionEvent e) {
        try {
            List<Product> productList = inventoryDAO.selectAllProducts();
            JComboBox<Product> productComboBox = new JComboBox<>(productList.toArray(new Product[0]));
            JOptionPane.showMessageDialog(null, productComboBox, "Select Product", JOptionPane.QUESTION_MESSAGE);
            Product selectedProduct = (Product) productComboBox.getSelectedItem();

            if (selectedProduct != null) {
                int inventoryStock = selectedProduct.getQuantity(); // Current inventory stock
                int purchasedStock = Integer.parseInt(JOptionPane.showInputDialog(this, "Enter Purchased Stock:"));
                double costPrice = Double.parseDouble(JOptionPane.showInputDialog(this, "Enter Cost Price:"));
                double amount = purchasedStock * costPrice;

                int totalStockAtPurchase = inventoryStock + purchasedStock;
                Purchase purchase = new Purchase(selectedProduct.getProductID(), selectedProduct.getName(), inventoryStock, purchasedStock, costPrice, amount, totalStockAtPurchase);
                inventoryDAO.insertPurchase(purchase);
                refreshPurchasesData();
            } else {
                JOptionPane.showMessageDialog(this, "Product not found", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (NumberFormatException | SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }





    private void editPurchase(ActionEvent e) {
        int selectedRow = purchasesTable.getSelectedRow();
        if (selectedRow >= 0) {
            int productID = (Integer) purchasesModel.getValueAt(selectedRow, 0); // Retrieve ProductID from the table

            try {
                Purchase existingPurchase = inventoryDAO.selectPurchase(productID); // Fetch the existing purchase details
                int originalQuantityPurchased = existingPurchase.getPurchasedStock(); // Store the original purchased quantity

                // Now show a dialog or form to the user to edit these details
                String productName = (String) JOptionPane.showInputDialog(this, "Edit Product Name:", existingPurchase.getProductName());
                int inventoryStock = Integer.parseInt(JOptionPane.showInputDialog(this, "Edit Inventory Stock:", existingPurchase.getInventoryStock()));
                int purchasedStock = Integer.parseInt(JOptionPane.showInputDialog(this, "Edit Purchased Stock:", existingPurchase.getPurchasedStock()));
                double costPrice = Double.parseDouble(JOptionPane.showInputDialog(this, "Edit Cost Price:", existingPurchase.getCostPrice()));
                double amount = purchasedStock * costPrice; // Calculate the amount based on the new purchased stock and cost price

                // Update the existing purchase object
                existingPurchase.setProductName(productName);
                existingPurchase.setInventoryStock(inventoryStock);
                existingPurchase.setPurchasedStock(purchasedStock);
                existingPurchase.setCostPrice(costPrice);
                existingPurchase.setAmount(amount);

                // Pass both the updated purchase and the original quantity to the updatePurchase method
                inventoryDAO.updatePurchase(existingPurchase, originalQuantityPurchased); // Update the purchase in the database

                refreshPurchasesData(); // Refresh the data in the UI
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Invalid number format", "Error", JOptionPane.ERROR_MESSAGE);
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Error updating purchase: " + ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
            }
        } else {
            JOptionPane.showMessageDialog(this, "Please select a purchase to edit.");
        }
    }





    private void deletePurchase(ActionEvent e) {
        int selectedRow = purchasesTable.getSelectedRow();
        if (selectedRow >= 0) {
            int productID = (Integer) purchasesModel.getValueAt(selectedRow, 0); // Retrieve ProductID from the table

            int confirm = JOptionPane.showConfirmDialog(this, "Are you sure you want to delete this purchase?", "Delete Purchase", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                try {
                    inventoryDAO.deletePurchase(productID); // Delete the purchase from the database
                    refreshPurchasesData(); // Refresh the data in the UI
                } catch (SQLException ex) {
                    JOptionPane.showMessageDialog(this, "Error deleting purchase: " + ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        } else {
            JOptionPane.showMessageDialog(this, "Please select a purchase to delete.");
        }
    }


}