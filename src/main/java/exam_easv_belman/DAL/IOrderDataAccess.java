package exam_easv_belman.DAL;

import exam_easv_belman.BE.Order;
import javafx.collections.ObservableList;

import java.sql.SQLException;

public interface IOrderDataAccess {

     ObservableList<Order> getAllOrders() throws  SQLException;

     String getEmailForOrder(String orderNumber) throws SQLException;

     void addCommentToOrder(String comment, String orderNumber) throws SQLException;

     String getCommentForOrder(String orderNumber) throws SQLException;


}
