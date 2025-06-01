package exam_easv_belman.DAL;

import exam_easv_belman.GUI.util.AlertHelper;
import javafx.scene.control.Alert;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class OrderDAO implements IOrderDataAccess {
    private DBConnector dbConnector;

    public OrderDAO() throws Exception {
        dbConnector = new DBConnector();
    }

    /**
     * Retrieves the customer's email address for a given order number.
     * This method queries the database for the customer_email associated
     * with the specified orderNumber in the dbo.Orders table.
     *
     * @param orderNumber The order number for which to retrieve the email address.
     * @return The customer's email address as a String if found, otherwise null.
     * @throws SQLException If a database access error occurs or if the connection to the database fails. The exception will include a descriptive message and the original SQL exception.
     */
    @Override
    public String getEmailForOrder(String orderNumber) throws SQLException {
        String sql = "SELECT customer_email FROM dbo.Orders WHERE order_number = ?";
        
        try (Connection conn = dbConnector.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ) {
            ps.setString(1, orderNumber);
            
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("customer_email");
                }
                return null;
            }
        } catch (SQLException e) {
            throw new SQLException("Error retrieving email for order: " + orderNumber, e);
        }
    }

    /**
     * Adds or updates a comment for a specific order in the database.
     *
     * This method executes an SQL UPDATE statement to set the comment
     * field for the order identified by orderNumber in the
     * dbo.Orders table.
     *
     * @param comment     The comment text to be added to the order.
     * @param orderNumber The unique identifier of the order to update.
     * @throws SQLException If a database access error occurs.
     */
    @Override
    public void addCommentToOrder(String comment, String orderNumber) throws SQLException {
        String sql = "UPDATE dbo.Orders SET comment = ? WHERE order_number = ?";

        try (Connection conn = dbConnector.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ) {
            ps.setString(1, comment);
            ps.setString(2, orderNumber);

            ps.executeUpdate();
            System.out.println("coment added?");
        } catch (SQLException e) {
            AlertHelper.showAlert("DB call error", "Could not add comment to order", Alert.AlertType.ERROR);
        }


    }

    /**
     * Retrieves the comment associated with a specific order number from the database.
     * This method queries the dbo.Orders table for the Comment
     * field corresponding to the provided orderNumber.
     *
     * @param orderNumber The unique identifier of the order for which to retrieve the comment.
     * @return The comment as a String if one exists for the order, otherwise null.
     * @throws SQLException If a database access error occurs during the query
     */
    @Override
    public String getCommentForOrder(String orderNumber) throws SQLException {
        String sql = "SELECT Comment FROM dbo.Orders WHERE order_number = ?";
        try (Connection conn = dbConnector.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);) {
            ps.setString(1, orderNumber);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("Comment");
                }
                return null;
            }
             }
    }
}