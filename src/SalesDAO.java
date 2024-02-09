import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SalesDAO {
    private String jdbcURL = "jdbc:mysql://localhost:3306/myCafeteriaDB";
    private String jdbcUsername = "root";
    private String jdbcPassword = "daksh0915@";
    private InventoryDAO inventoryDAO;
    private static final String INSERT_SALES_SQL = "INSERT INTO sales (ProductID, ProductName, StockSold, SellingPrice) VALUES (?, ?, ?, ?);";
    private static final String SELECT_ALL_SALES = "SELECT s.SaleID, s.ProductID, s.ProductName, s.StockSold, s.SellingPrice, p.Quantity AS TotalStock FROM sales s INNER JOIN products p ON s.ProductID = p.ProductID";


    private static final String DELETE_SALES_SQL = "DELETE FROM sales WHERE ProductID = ?;";
    private static final String UPDATE_SALES_SQL = "UPDATE sales SET ProductName = ?, StockSold = ?, SellingPrice = ? WHERE ProductID = ?;";
    private static final String SELECT_SALES_BY_ID = "SELECT s.SaleID, s.ProductID, s.ProductName, s.StockSold, s.SellingPrice, p.Quantity AS TotalStock FROM sales s INNER JOIN products p ON s.ProductID = p.ProductID WHERE s.ProductID = ?;";

    public SalesDAO(InventoryDAO inventoryDAO) {
        this.inventoryDAO = inventoryDAO;
    }

    protected Connection getConnection() throws SQLException {
        return DriverManager.getConnection(jdbcURL, jdbcUsername, jdbcPassword);
    }

    public void insertSale(Sale sale) throws SQLException {
        Connection connection = null;
        PreparedStatement saleStatement = null;
        try {
            connection = getConnection();
            connection.setAutoCommit(false); // Start transaction

            saleStatement = connection.prepareStatement(INSERT_SALES_SQL, Statement.RETURN_GENERATED_KEYS);
            saleStatement.setInt(1, sale.getProductID());
            saleStatement.setString(2, sale.getProductName());
            saleStatement.setInt(3, sale.getStockSold());
            saleStatement.setDouble(4, sale.getSellingPrice());
            int affectedRows = saleStatement.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Inserting sale failed, no rows affected.");
            }
            try (ResultSet generatedKeys = saleStatement.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    long saleId = generatedKeys.getLong(1);
                    System.out.println("Inserted sale with ID: " + saleId);
                } else {
                    throw new SQLException("Inserting sale failed, no ID obtained.");
                }
            }
            connection.commit(); // Commit transaction
            System.out.println("Transaction committed successfully.");
        } catch (SQLException e) {
            System.err.println("SQLState: " + e.getSQLState());
            System.err.println("Error Code: " + e.getErrorCode());
            System.err.println("Message: " + e.getMessage());
            if (connection != null) {
                try {
                    connection.rollback(); // Rollback transaction on error
                } catch (SQLException exRollback) {
                    exRollback.printStackTrace();
                }
            }
            throw e; // Rethrow the exception
        } finally {
            if (saleStatement != null) {
                saleStatement.close();
            }
            if (connection != null) {
                connection.close();
            }
        }
    }




    public List<Sale> selectAllSales() throws SQLException {
        List<Sale> sales = new ArrayList<>();
        try (Connection connection = getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(SELECT_ALL_SALES)) {
            ResultSet rs = preparedStatement.executeQuery();
            while (rs.next()) {
                int saleID = rs.getInt("SaleID");
                int productID = rs.getInt("ProductID");
                String productName = rs.getString("ProductName");
                int stockSold = rs.getInt("StockSold");
                double sellingPrice = rs.getDouble("SellingPrice");
                int totalStock = rs.getInt("TotalStock"); // Retrieve the total stock from the result set

                sales.add(new Sale(productID, productName, stockSold, totalStock, sellingPrice)); // Include totalStock in the constructor
            }
        }
        return sales;
    }


    public boolean updateSale(Sale sale, int originalQuantitySold) throws SQLException {
        boolean rowUpdated;
        Connection connection = null;
        PreparedStatement statement = null;
        try {
            connection = getConnection();
            connection.setAutoCommit(false); // Start transaction

            statement = connection.prepareStatement(UPDATE_SALES_SQL);
            statement.setString(1, sale.getProductName());
            statement.setInt(2, sale.getStockSold());
            statement.setDouble(3, sale.getSellingPrice());
            statement.setInt(4, sale.getProductID());

            rowUpdated = statement.executeUpdate() > 0;

            if (rowUpdated) {
                // Calculate the change in stock sold
                int stockChange = sale.getStockSold() - originalQuantitySold;

                inventoryDAO.changeProductStock(connection, sale.getProductID(), Math.abs(stockChange), stockChange < 0);
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
            throw e; // Rethrow the exception
        } finally {
            if (statement != null) statement.close();
            if (connection != null) connection.close();
        }
        return rowUpdated;
    }



    public Sale selectSale(int productID) throws SQLException {
        Sale sale = null;
        try (Connection connection = getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(SELECT_SALES_BY_ID)) {
            preparedStatement.setInt(1, productID);
            ResultSet rs = preparedStatement.executeQuery();
            if (rs.next()) {
                int saleID = rs.getInt("SaleID");
                String productName = rs.getString("ProductName");
                int stockSold = rs.getInt("StockSold");
                double sellingPrice = rs.getDouble("SellingPrice");
                int totalStock = rs.getInt("TotalStock"); // Retrieve the total stock from the result set

                sale = new Sale(productID, productName, stockSold, totalStock, sellingPrice);
            }
        }
        return sale;
    }


    public boolean deleteSale(int productID) throws SQLException {
        boolean rowDeleted;
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(DELETE_SALES_SQL)) {
            statement.setInt(1, productID);
            rowDeleted = statement.executeUpdate() > 0;
        }
        return rowDeleted;
    }
    public int calculateBalanceStock(int productID) throws SQLException {
        int balanceStock = 0;
        String sql = "SELECT TotalStock, StockSold FROM sales WHERE ProductID = ?";

        try (Connection connection = getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setInt(1, productID);
            ResultSet rs = preparedStatement.executeQuery();
            if (rs.next()) {
                int totalStock = rs.getInt("TotalStock");
                int stockSold = rs.getInt("StockSold");
                balanceStock = totalStock - stockSold;
            }
        } catch (SQLException e) {
            printSQLException(e);
        }
        return balanceStock;
    }

    private void closeConnection(AutoCloseable resource) {
        try {
            if (resource != null) {
                resource.close();
            }
        } catch (Exception ex) {
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
                Throwable t = ex.getCause();
                while (t != null) {
                    System.err.println("Cause: " + t);
                    t = t.getCause();
                }
            }
        }
    }

}