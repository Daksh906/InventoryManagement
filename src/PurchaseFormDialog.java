import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.List;

public class PurchaseFormDialog extends JDialog {
    private JComboBox<Product> productComboBox;
    private JTextField quantityField;
    private JTextField costPriceField;
    private JButton saveButton;
    private JButton cancelButton;
    private boolean saved;

    public PurchaseFormDialog(Window owner, List<Product> products) {
        super(owner, ModalityType.APPLICATION_MODAL);
        setTitle("Purchase Form");
        setSize(300, 200);
        setLayout(new GridLayout(4, 2));

        add(new JLabel("Product:"));
        productComboBox = new JComboBox<>(new DefaultComboBoxModel<>(products.toArray(new Product[0])));
        add(productComboBox);

        add(new JLabel("Quantity:"));
        quantityField = new JTextField();
        add(quantityField);

        add(new JLabel("Cost Price:"));
        costPriceField = new JTextField();
        add(costPriceField);

        saveButton = new JButton("Save");
        saveButton.addActionListener(this::saveButtonAction);
        add(saveButton);

        cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(this::cancelButtonAction);
        add(cancelButton);
    }

    private void saveButtonAction(ActionEvent e) {
        // Validate input and save purchase
        saved = true;
        dispose();
    }

    private void cancelButtonAction(ActionEvent e) {
        saved = false;
        dispose();
    }

    public boolean showDialog() {
        setVisible(true);
        return saved;
    }

    public Product getSelectedProduct() {
        return (Product) productComboBox.getSelectedItem();
    }

    public int getQuantity() {
        try {
            return Integer.parseInt(quantityField.getText());
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    public double getCostPrice() {
        try {
            return Double.parseDouble(costPriceField.getText());
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }

    // Method to set fields when editing
    public void setFields(Product product, int quantity, double costPrice) {
        productComboBox.setSelectedItem(product);
        quantityField.setText(Integer.toString(quantity));
        costPriceField.setText(Double.toString(costPrice));
    }
}