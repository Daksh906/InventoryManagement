public class Purchase {
    private int productID;
    private String productName;
    private int inventoryStock;
    private int purchasedStock;
    private double costPrice;
    private double amount;
    private int totalStock;

    // Constructor
    public Purchase(int productID, String productName, int inventoryStock, int purchasedStock, double costPrice, double amount, int totalStock) {
        this.productID = productID;
        this.productName = productName;
        this.inventoryStock = inventoryStock;
        this.purchasedStock = purchasedStock;
        this.costPrice = costPrice;
        this.amount = amount;
        this.totalStock = totalStock;
        calculateTotalStock();
    }

    // Calculate total stock based on inventory and purchased stock
    private void calculateTotalStock() {
        this.totalStock = this.inventoryStock + this.purchasedStock;
    }

    // Getters and Setters, with logic to recalculate total stock if inventory or purchased stock changes
    public int getProductID() {
        return productID;
    }

    public void setProductID(int productID) {
        this.productID = productID;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public int getInventoryStock() {
        return inventoryStock;
    }

    public void setInventoryStock(int inventoryStock) {
        this.inventoryStock = inventoryStock;
        calculateTotalStock();
    }

    public int getPurchasedStock() {
        return purchasedStock;
    }

    public void setPurchasedStock(int purchasedStock) {
        this.purchasedStock = purchasedStock;
        calculateTotalStock();
    }

    public int getTotalStock() {
        return totalStock;
    }

    // New setter for totalStock
    public void setTotalStock(int totalStock) {
        this.totalStock = totalStock;

    }
    public double getCostPrice () {
        return costPrice;
    }

    public void setCostPrice ( double costPrice){
        this.costPrice = costPrice;
    }

    public double getAmount () {
        return amount;
    }

    public void setAmount ( double amount){
        this.amount = amount;
    }
}