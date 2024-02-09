public class Sale {
    private int saleID; // Added to store SaleID from the database
    private int productID;
    private String productName;
    private int stockSold;
    private double sellingPrice;
    private int totalStock; // New attribute for total stock

    // Constructor
    public Sale(int productID, String productName, int stockSold, int totalStock, double sellingPrice) {
        this.productID = productID;
        this.productName = productName;
        this.stockSold = stockSold;
        this.sellingPrice = sellingPrice;
        this.totalStock = totalStock; // Initialize the total stock
    }

    // Getters
    public int getSaleID() {
        return saleID;
    }

    public int getProductID() {
        return productID;
    }

    public String getProductName() {
        return productName;
    }

    public int getStockSold() {
        return stockSold;
    }

    public double getSellingPrice() {
        return sellingPrice;
    }

    public int getTotalStock() { // Getter for total stock
        return totalStock;
    }

    // Setters

    public void setProductID(int productID) {
        this.productID = productID;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public void setStockSold(int stockSold) {
        this.stockSold = stockSold;
    }

    public void setSellingPrice(double sellingPrice) {
        this.sellingPrice = sellingPrice;
    }

    public void setTotalStock(int totalStock) { // Setter for total stock
        this.totalStock = totalStock;
    }

    // Additional methods
    public double calculateSalesRevenue() {
        // This method calculates the sales revenue based on stock sold and selling price.
        return stockSold * sellingPrice;
    }
}
