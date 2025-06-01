package exam_easv_belman.BLL;

import exam_easv_belman.BE.Order;
import exam_easv_belman.DAL.IOrderDataAccess;
import exam_easv_belman.DAL.OrderDAO;
import javafx.collections.ObservableList;

import java.sql.SQLException;

public class OrderManager {

    private IOrderDataAccess orderDataAccess;

    public OrderManager() throws Exception {
     orderDataAccess = new OrderDAO();
    }

    public String getEmailForOrder(String orderNumber) throws SQLException {
        return orderDataAccess.getEmailForOrder(orderNumber);
    }

    public void addCommentToOrder(String comment, String orderNumber) throws SQLException {
        orderDataAccess.addCommentToOrder(comment, orderNumber);
    }

    public String getCommentForOrder(String orderNumber) throws SQLException {
        return orderDataAccess.getCommentForOrder(orderNumber);
    }

    public ObservableList<Order> getAllOrders() throws SQLException {
        return orderDataAccess.getAllOrders();
    }
}
