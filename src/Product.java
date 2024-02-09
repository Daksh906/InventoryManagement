public class Product {
    private int productID;
    private String name;
    private double price;
    private int quantity;
    private String imagePath;

    // Constructor
    public Product(int productID, String name, double price, int quantity, String imagePath) {
        this.productID = productID;
        this.name = name;
        this.price = price;
        this.quantity = quantity;
        this.imagePath = imagePath;
    }

    // Getters
    public int getProductID() { return productID; }
    public String getName() { return name; }
    public double getPrice() { return price; }
    public int getQuantity() { return quantity; }
    public String getImagePath() { return imagePath; }

    // Setters
    public void setProductID(int productID) {
        this.productID = productID;
    }
    public void setName(String name) {
        this.name = name;
    }
    public void setPrice(double price) {
        this.price = price;
    }
    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }
    @Override
    public String toString() {
        return name; // This will return the product's name when the product object is printed or rendered in a UI component.
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }

    // Decrease the quantity of the product
    public void decreaseQuantity(int amount) {
        if (quantity >= amount) {
            quantity -= amount;
        } else {
            throw new IllegalArgumentException("Insufficient product quantity");
        }
    }
}