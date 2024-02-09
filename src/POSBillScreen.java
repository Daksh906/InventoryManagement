import javax.swing.*;
import java.awt.*;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import javax.swing.Timer;
public class POSBillScreen extends JFrame {
    private StaffDashboard staffDashboard;

    private JTextField orderIdField;
    private JTextField showTimeField;
    private JTextField orderDateField;
    private JTextField numberOfItemsField;
    private JTextField totalAmountField;
    private JComboBox<String> modeOfPaymentComboBox;
    private List<LineItem> lineItems;
    private BigDecimal totalAmount;
    private JPanel lineItemsPanel;

    public POSBillScreen(StaffDashboard staffDashboard) {
        this.staffDashboard = staffDashboard; // Initialize StaffDashboard reference
        lineItems = new ArrayList<>();
        totalAmount = BigDecimal.ZERO;

        setTitle("Customer Bill Page");
        setSize(800, 600);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));

        orderIdField = new JTextField(generateOrderID());
        showTimeField = new JTextField(new SimpleDateFormat("HH:mm:ss").format(new Date()));
        orderDateField = new JTextField(new SimpleDateFormat("dd/MM/yyyy").format(new Date()));
        numberOfItemsField = new JTextField("0");
        totalAmountField = new JTextField("0.00");
        modeOfPaymentComboBox = new JComboBox<>(new String[]{"Cash", "Credit Card", "Mobile Pay"});

        setupDateTimeFields(showTimeField, orderDateField);

        JPanel northPanel = createTopPanel();
        JPanel centerPanel = createCenterPanel();
        JPanel southPanel = createSouthPanel();

        add(northPanel, BorderLayout.NORTH);
        add(centerPanel, BorderLayout.CENTER);
        add(southPanel, BorderLayout.SOUTH);

        lineItemsPanel = new JPanel();
        lineItemsPanel.setLayout(new BoxLayout(lineItemsPanel, BoxLayout.Y_AXIS));
        JScrollPane scrollPane = new JScrollPane(lineItemsPanel);
        add(scrollPane, BorderLayout.EAST);


        JButton printButton = new JButton("Print Bill");
        printButton.addActionListener(e -> printBill());
        southPanel.add(printButton);

        setLocationRelativeTo(null);
    }
    private void printBill() {
        PrinterJob job = PrinterJob.getPrinterJob();
        job.setPrintable(new Printable() {
            @Override
            public int print(Graphics graphics, PageFormat pageFormat, int pageIndex) {
                if (pageIndex > 0) {
                    return NO_SUCH_PAGE;
                }

                Graphics2D g2d = (Graphics2D) graphics;
                g2d.translate(pageFormat.getImageableX(), pageFormat.getImageableY());
                g2d.setFont(new Font("Monospaced", Font.PLAIN, 12));

                int y = 15;
                g2d.drawString("Order ID: " + orderIdField.getText(), 100, y); y += 15;
                g2d.drawString("Date: " + orderDateField.getText(), 100, y); y += 15;
                g2d.drawString("Time: " + showTimeField.getText(), 100, y); y += 15;
                g2d.drawString("Total Items: " + numberOfItemsField.getText(), 100, y); y += 15;
                g2d.drawString("Total Amount: $" + totalAmountField.getText(), 100, y); y += 15;
                g2d.drawString("Items:", 100, y); y += 15;

                for (LineItem item : lineItems) {
                    g2d.drawString(item.getProduct().getName() + " - Qty: " + item.getQuantity(), 100, y); y += 15;
                }

                return PAGE_EXISTS;
            }
        });

        boolean doPrint = job.printDialog();
        if (doPrint) {
            try {
                job.print();
            } catch (PrinterException e) {
                JOptionPane.showMessageDialog(this, "Printing error: " + e.getMessage(), "Print Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }


    private JButton createBackButton() {
        JButton backButton = new JButton("Back to Dashboard");
        backButton.addActionListener(e -> {
            this.setVisible(false);
            staffDashboard.setVisible(true);
        });
        return backButton;
    }

    private JPanel createTopPanel() {
        JPanel northPanel = new JPanel(new GridLayout(1, 3, 5, 5));
        northPanel.add(createLabeledField("Order ID:", orderIdField));
        northPanel.add(createLabeledField("Show Time:", showTimeField));
        northPanel.add(createLabeledField("Order Date:", orderDateField));
        return northPanel;
    }

    private JPanel createCenterPanel() {
        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new GridLayout(2, 1, 5, 5));
        centerPanel.add(createLabeledField("No of Items:", numberOfItemsField));
        centerPanel.add(createLabeledField("Total Amount:", totalAmountField));
        return centerPanel;
    }

    private JPanel createSouthPanel() {
        JPanel southPanel = new JPanel();
        southPanel.add(new JLabel("Mode of Payment:"));
        southPanel.add(modeOfPaymentComboBox);
        southPanel.add(createBackButton());
        return southPanel;
    }

    private JPanel createLabeledField(String labelText, JTextField field) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(new JLabel(labelText), BorderLayout.NORTH);
        panel.add(field, BorderLayout.CENTER);
        field.setEditable(false);
        return panel;
    }

    private void setupDateTimeFields(JTextField timeField, JTextField dateField) {
        Timer timer = new Timer(1000, e -> {
            timeField.setText(new SimpleDateFormat("HH:mm:ss").format(new Date()));
            dateField.setText(new SimpleDateFormat("dd/MM/yyyy").format(new Date()));
        });
        timer.start();
    }

    private String generateOrderID() {
        return "ORD" + new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
    }

    public void addItemToOrder(Product product, int quantity) {
        LineItem item = new LineItem(product, quantity);
        lineItems.add(item);
        updateTotals();
        updateLineItemsPanel();
    }

    private void updateTotals() {
        totalAmount = lineItems.stream()
                .map(item -> BigDecimal.valueOf(item.getProduct().getPrice())
                        .multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        numberOfItemsField.setText(String.valueOf(lineItems.size()));
        totalAmountField.setText(totalAmount.toPlainString());
    }

    private void updateLineItemsPanel() {
        lineItemsPanel.removeAll();
        for (LineItem item : lineItems) {
            JLabel label = new JLabel(item.getProduct().getName() + " - Qty: " + item.getQuantity());
            lineItemsPanel.add(label);
        }
        lineItemsPanel.revalidate();
        lineItemsPanel.repaint();
    }




    class LineItem {
        private Product product;
        private int quantity;

        public LineItem(Product product, int quantity) {
            this.product = product;
            this.quantity = quantity;
        }

        public Product getProduct() {
            return product;
        }

        public int getQuantity() {
            return quantity;
        }
    }
}