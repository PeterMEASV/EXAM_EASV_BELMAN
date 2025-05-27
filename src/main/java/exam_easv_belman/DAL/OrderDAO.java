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