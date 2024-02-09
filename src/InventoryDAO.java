import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class InventoryDAO {
    private String jdbcURL = "jdbc:mysql://localhost:3306/myCafeteriaDB";
    private String jdbcUsername = "root";
    private String jdbcPassword = "daksh0915@";

    private static final String INSERT_PRODUCTS_SQL = "INSERT INTO products (ProductName, Price, Quantity, ImagePath) VALUES (?, ?, ?, ?);";
    private static final String SELECT_PRODUCT_BY_ID = "select ProductID,ProductName,Price,Quantity,ImagePath from products where ProductID =?";
    private static final String SELECT_ALL_PRODUCTS = "select * from products";
    private static final String DELETE_PRODUCTS_SQL = "delete from products where ProductID = ?;";
    private static final String UPDATE_PRODUCTS_SQL = "update products set ProductName = ?,Price= ?, Quantity =?, ImagePath =? where ProductID = ?;";
    private static final String INSERT_PURCHASE_SQL = "INSERT INTO purchases (ProductID, ProductName, InventoryStock, PurchasedStock, CostPrice, Amount) VALUES (?, ?, ?, ?, ?, ?);";
    private static final String SELECT_ALL_PURCHASES = "SELECT * FROM purchases";
    private static final String UPDATE_PURCHASE_SQL = "UPDATE purchases SET ProductName = ?, InventoryStock = ?, PurchasedStock = ?, CostPrice = ?, Amount = ? WHERE ProductID = ?;";
    private static final String DELETE_PURCHASE_SQL = "DELETE FROM purchases WHERE ProductID = ?;";
    private static final String SELECT_PURCHASE_BY_ID = "SELECT * FROM purchases WHERE ProductID = ?;";
    private static final String PURCHASE_TRANSATION = "START TRANSACTION;\n" +
            "SET @ProductId = ?;\n" +
            "SET @ProductName = ?;\n" +
            "SET @PurchasedStock = ?;\n" +
            "SET @CostPrice = ?;\n" +
            "SELECT Quantity INTO @AvailableQuantity FROM products WHERE ID = @ProductId;\n" +
            "IF @AvailableQuantity >= @PurchasedStock THEN\n" +
            "    INSERT INTO purchases (ProductID, ProductName, PurchasedStock, CostPrice)\n" +
            "    VALUES (@ProductId, @ProductName, @PurchasedStock, @CostPrice);\n" +
            "    UPDATE products SET Quantity = Quantity - @PurchasedStock WHERE ID = @ProductId;\n" +
            "    COMMIT;\n" +
            "ELSE\n" +
            "    ROLLBACK;\n" +
            "END IF;\n";


    protected Connection getConnection() throws SQLException {
        return DriverManager.getConnection(jdbcURL, jdbcUsername, jdbcPassword);
    }
    public void changeProductStock(Connection conn, int productId, int quantityChange, boolean increase) throws SQLException {
        String sql = "UPDATE products SET Quantity = Quantity " + (increase ? "+" : "-") + " ? WHERE ProductID = ?;";
        try (PreparedStatement statement = conn.prepareStatement(sql)) {
            statement.setInt(1, Math.abs(quantityChange));
            statement.setInt(2, productId);
            statement.executeUpdate();
        }
    }

    /* public void updateStockAfterSale(int productID, int quantitySold) throws SQLException {
        try (Connection connection = getConnection()) {
            connection.setAutoCommit(false);
            try {
                // Decrease product stock
                changeProductStock(connection, productID, quantitySold, false);
                // Update total stock in purchases
                updatePurchaseTotalStock(connection, productID);
                connection.commit();
            } catch (SQLException e) {
                connection.rollback();
                throw e;
            }
        }
    }*/
    public void insertProduct(Product product) throws SQLException {
        try (Connection connection = getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(INSERT_PRODUCTS_SQL)) {
            preparedStatement.setString(1, product.getName());
            preparedStatement.setDouble(2, product.getPrice());
            preparedStatement.setInt(3, product.getQuantity());
            preparedStatement.setString(4, product.getImagePath());
            preparedStatement.executeUpdate();
        }
    }

    public boolean updateProduct(Product product) throws SQLException {
        boolean rowUpdated;
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(UPDATE_PRODUCTS_SQL)) {
            statement.setString(1, product.getName());
            statement.setDouble(2, product.getPrice());
            statement.setInt(3, product.getQuantity());
            statement.setString(4, product.getImagePath());
            statement.setInt(5, product.getProductID());

            rowUpdated = statement.executeUpdate() > 0;
        }
        return rowUpdated;
    }

    public Product selectProduct(int id) throws SQLException {
        Product product = null;
        try (Connection connection = getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(SELECT_PRODUCT_BY_ID)) {
            preparedStatement.setInt(1, id);
            try (ResultSet rs = preparedStatement.executeQuery()) {
                while (rs.next()) {
                    String name = rs.getString("ProductName");
                    double price = rs.getDouble("Price");
                    int quantity = rs.getInt("Quantity");
                    String imagePath = rs.getString("ImagePath");
                    product = new Product(id, name, price, quantity, imagePath);
                }
            }
        }
        return product;
    }

    public List<Product> selectAllProducts() throws SQLException {
        List<Product> products = new ArrayList<>();
        try (Connection connection = getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(SELECT_ALL_PRODUCTS);
             ResultSet rs = preparedStatement.executeQuery()) {

            while (rs.next()) {
                int id = rs.getInt("ProductID");
                String name = rs.getString("ProductName");
                double price = rs.getDouble("Price");
                int quantity = rs.getInt("Quantity");
                String imagePath = rs.getString("ImagePath");
                products.add(new Product(id, name, price, quantity, imagePath));
            }
        }
        return products;
    }

    public boolean deleteProduct(int id) throws SQLException {
        boolean rowDeleted;
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(DELETE_PRODUCTS_SQL)) {
            statement.setInt(1, id);
            rowDeleted = statement.executeUpdate() > 0;
        }
        return rowDeleted;
    }


    public void insertPurchase(Purchase purchase) throws SQLException {
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        try {
            connection = getConnection();
            connection.setAutoCommit(false); // Start transaction

            preparedStatement = connection.prepareStatement(INSERT_PURCHASE_SQL);
            preparedStatement.setInt(1, purchase.getProductID());
            preparedStatement.setString(2, purchase.getProductName());
            preparedStatement.setInt(3, purchase.getInventoryStock());
            preparedStatement.setInt(4, purchase.getPurchasedStock());
            preparedStatement.setDouble(5, purchase.getCostPrice());
            preparedStatement.setDouble(6, purchase.getAmount());
            int affectedRows = preparedStatement.executeUpdate();

            if (affectedRows == 0) {
                throw new SQLException("Creating purchase failed, no rows affected.");
            }
            connection.commit(); // Commit transaction
        } catch (SQLException e) {
            if (connection != null) {
                try {
                    connection.rollback(); // Rollback transaction on error
                } catch (SQLException exRollback) {
                    exRollback.printStackTrace();
                }
            }
            throw e; // Rethrow the exception to be handled further up the call stack
        } finally {

            if (preparedStatement != null) {
                preparedStatement.close();
            }
            if (connection != null) {
                connection.close();
            }
        }
    }





    public boolean updatePurchase(Purchase purchase, int originalQuantityPurchased) throws SQLException {
        boolean rowUpdated;
        Connection connection = null;
        PreparedStatement statement = null;
        try {
            connection = getConnection();
            connection.setAutoCommit(false); // Start transaction

            // Update the purchase record with the new details
            statement = connection.prepareStatement(UPDATE_PURCHASE_SQL);
            statement.setString(1, purchase.getProductName());
            statement.setInt(2, purchase.getInventoryStock());
            statement.setInt(3, purchase.getPurchasedStock());
            statement.setDouble(4, purchase.getCostPrice());
            statement.setDouble(5, purchase.getAmount());
            statement.setInt(6, purchase.getProductID());

            rowUpdated = statement.executeUpdate() > 0;

            if (rowUpdated) {
                // Calculate the difference in purchased quantity
                int purchasedQuantityDifference = purchase.getPurchasedStock() - originalQuantityPurchased;

                // Adjust the product stock accordingly
                // If the new purchase quantity is greater, we increase the stock
                // If the new purchase quantity is less, we decrease the stock
                 changeProductStock(connection, purchase.getProductID(), purchasedQuantityDifference, purchasedQuantityDifference > 0);
            }

            connection.commit(); // Commit transaction
        } catch (SQLException e) {
            if (connection != null) {
                connection.rollback(); // Rollback transaction on error
            }
            throw e; // Rethrow the exception
        } finally {
            if (statement != null) statement.close();
            if (connection != null) connection.close();
        }
        return rowUpdated;
    }


    public Purchase selectPurchase(int productID) throws SQLException {
        Purchase purchase = null;
        try (Connection connection = getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(SELECT_PURCHASE_BY_ID)) {
            preparedStatement.setInt(1, productID);
            ResultSet rs = preparedStatement.executeQuery();

            if (rs.next()) {
                int id = rs.getInt("ProductID");
                String name = rs.getString("ProductName");
                int inventoryStock = rs.getInt("InventoryStock");
                int purchasedStock = rs.getInt("PurchasedStock");
                double costPrice = rs.getDouble("CostPrice");
                double amount = rs.getDouble("Amount");
                int totalStock = rs.getInt("TotalStock");
                purchase = new Purchase(id, name, inventoryStock, purchasedStock, costPrice, amount, totalStock);
            }
        }
        return purchase;
    }


    public List<Purchase> selectAllPurchases() throws SQLException {
        List<Purchase> purchases = new ArrayList<>();
        try (Connection connection = getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(SELECT_ALL_PURCHASES);
             ResultSet rs = preparedStatement.executeQuery()) {

            while (rs.next()) {
                int productID = rs.getInt("ProductID");
                String productName = rs.getString("ProductName");
                int inventoryStock = rs.getInt("InventoryStock");
                int purchasedStock = rs.getInt("PurchasedStock");
                double costPrice = rs.getDouble("CostPrice");
                double amount = rs.getDouble("Amount");
                int totalStock = rs.getInt("TotalStock");
                purchases.add(new Purchase(productID, productName, inventoryStock, purchasedStock, costPrice, amount, totalStock));
            }
        }
        return purchases;
    }

    public boolean deletePurchase(int productID) throws SQLException {
        boolean rowDeleted;
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(DELETE_PURCHASE_SQL)) {
            statement.setInt(1, productID);
            rowDeleted = statement.executeUpdate() > 0;
        }
        return rowDeleted;
    }

    public void updatePurchaseTotalStock(Connection conn, int productID) throws SQLException {
        String sql = "UPDATE purchases SET TotalStock = (SELECT Quantity FROM products WHERE ProductID = ?) WHERE ProductID = ?;";
        try (PreparedStatement statement = conn.prepareStatement(sql)) {
            statement.setInt(1, productID);
            statement.setInt(2, productID);
            statement.executeUpdate();
        }
    }
    public ArrayList<Product> selectThreeProductsWithLowestQuantity() throws SQLException {
        ArrayList<Product> products = new ArrayList<>();
        String sql = "SELECT * FROM products ORDER BY Quantity ASC LIMIT 3"; // SQL is case-sensitive

        try (Connection connection = this.getConnection();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(sql)) {

            while (resultSet.next()) {
                int id = resultSet.getInt("ProductID"); // Make sure this matches your table's column name
                String name = resultSet.getString("ProductName"); // Make sure this matches your table's column name
                double price = resultSet.getDouble("Price"); // Make sure this matches your table's column name
                int quantity = resultSet.getInt("Quantity"); // Make sure this matches your table's column name
                String imagePath = resultSet.getString("ImagePath"); // Make sure this matches your table's column name

                Product product = new Product(id, name, price, quantity, imagePath);
                products.add(product);
            }
        }

        return products;
    }


    private void closeConnection(Connection conn, PreparedStatement stmt) {
        try {
            if (stmt != null) stmt.close();
            if (conn != null) conn.close();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    private void printSQLException(SQLException ex) {
        for (Throwable e : ex) {
            if (e instanceof SQLException) {
                e.printStackTrace(System.err);
                System.err.println("SQLState: " + ((SQLException) e).getSQLState());
                System.err.println("Error Code: " + ((SQLException) e).getErrorCode());
                System.err.println("Message: " + e.getMessage());
                Throwable t = e.getCause();
                while (t != null) {
                    System.err.println("Cause: " + t);
                    t = t.getCause();
                }
            }
        }
    }

}