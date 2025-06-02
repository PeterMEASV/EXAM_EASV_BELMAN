package exam_easv_belman.BLL;

import exam_easv_belman.BE.Order;
import exam_easv_belman.BLL.exceptions.BelmanBLLException;
import exam_easv_belman.DAL.IOrderDataAccess;
import exam_easv_belman.DAL.OrderDAO;
import javafx.collections.ObservableList;

import java.sql.SQLException;

public class OrderManager {

    private IOrderDataAccess orderDataAccess;

    public OrderManager() throws BelmanBLLException {
        try {
            orderDataAccess = new OrderDAO();
        } catch (Exception e) {
            throw new BelmanBLLException("Failed to initialize OrderDataAccess.", e);
        }
    }

    public String getEmailForOrder(String orderNumber) throws BelmanBLLException {
        try {
            return orderDataAccess.getEmailForOrder(orderNumber);
        } catch (SQLException e) {
            throw new BelmanBLLException("Failed to retrieve email for order: " + orderNumber, e);
        }
    }

    public void addCommentToOrder(String comment, String orderNumber) throws BelmanBLLException {
        try {
            orderDataAccess.addCommentToOrder(comment, orderNumber);
        } catch (SQLException e) {
            throw new BelmanBLLException("Failed to add comment to order: " + orderNumber, e);
        }
    }

    public String getCommentForOrder(String orderNumber) throws BelmanBLLException {
        try {
            return orderDataAccess.getCommentForOrder(orderNumber);
        } catch (SQLException e) {
            throw new BelmanBLLException("Failed to retrieve comment for order: " + orderNumber, e);
        }
    }

    public ObservableList<Order> getAllOrders() throws BelmanBLLException {
        try {
            return orderDataAccess.getAllOrders();
        } catch (SQLException e) {
            throw new BelmanBLLException("Failed to retrieve all orders.", e);
        }
    }
}
