package exam_easv_belman.DAL;

import exam_easv_belman.BE.Order;
import exam_easv_belman.BE.Product;
import exam_easv_belman.BLL.exceptions.BelmanDALException;
import exam_easv_belman.GUI.util.AlertHelper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Alert;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;

public class ProductDAO implements IProductDataAccess {
    private DBConnector dbConnector;

    public ProductDAO() throws Exception {
        dbConnector = new DBConnector();
    }

    /**
     * Gets all products from the database.
     * @return a list of all products in the database
     * @throws SQLException if a database access error occurs
     */
    @Override
    public ObservableList<Product> getAllProducts() throws SQLException {
        ObservableList<Product> products = FXCollections.observableArrayList();
        String sql = "SELECT * FROM dbo.Products";
        try(Connection conn = dbConnector.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)){
            ResultSet rs = ps.executeQuery();
            while (rs.next())
            {
                Product tempProduct = new Product();
                tempProduct.setId(rs.getInt("id"));
                tempProduct.setOrder_id(rs.getInt("order_id"));
                tempProduct.setProduct_number(rs.getString("products_order_number"));
                products.add(tempProduct);
            }
        }
        catch(SQLException e){
            AlertHelper.showAlert("DB error", "Could not get products from DB", Alert.AlertType.ERROR);
            throw new BelmanDALException("Error retrieving products from DB", e);
        }
        return products;
    }

    /**
     * Gets all products related to a specific order from the database.
     * @param orderNumber the order number for which to retrieve products
     * @return an ObservableList of Product objects
     * @throws SQLException if a database access error occurs
     */
    @Override
    public ObservableList<Product> getProductsForOrder(String orderNumber) throws SQLException {
        Order order = getOrderForNumber(orderNumber);
        ObservableList<Product> productsList = FXCollections.observableArrayList();
        String sql = "SELECT * FROM dbo.Products WHERE order_id = ?";
        try(Connection conn = dbConnector.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)){
            ps.setInt(1, order.getId());
            ResultSet rs = ps.executeQuery();
            while (rs.next())
            {
                Product tempProduct = new Product();
                tempProduct.setId(rs.getInt("id"));
                tempProduct.setOrder_id(rs.getInt("order_id"));
                tempProduct.setProduct_number(rs.getString("products_order_number"));
                productsList.add(tempProduct);
            }
        }
        catch(SQLException e){
            AlertHelper.showAlert("DB error", "Could not get products from DB", Alert.AlertType.ERROR);
            throw new BelmanDALException("Error retrieving products from DB", e);
        }
    return productsList;
    }

    /**
     * Gets an order from the database based on its order number.
     * @param orderNumber the order number for which to retrieve the order
     * @return the Order object if found, otherwise null
     * @throws SQLException if a database access error occurs
     */
    @Override
    public Order getOrderForNumber(String orderNumber) throws SQLException {
        Order tempOrder = new Order();
       String sql = "SELECT * FROM dbo.Orders WHERE order_number = ?";
        try(Connection conn = dbConnector.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)){
            ps.setString(1, orderNumber);
            ResultSet rs = ps.executeQuery();
            while (rs.next())
            {
                tempOrder.setId(rs.getInt("id"));
                tempOrder.setOrderNumber(rs.getString("order_number"));
                tempOrder.setCustomerEmail(rs.getString("customer_email"));
                tempOrder.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
                tempOrder.setComment(rs.getString("Comment"));
            }
        }
        catch(SQLException e){
            throw new BelmanDALException("Could not retrieve the order", e);
        }
        return tempOrder;
        }


}
