package exam_easv_belman.BLL;

import exam_easv_belman.BE.Product;
import exam_easv_belman.BLL.exceptions.BelmanBLLException;
import exam_easv_belman.DAL.IProductDataAccess;
import exam_easv_belman.DAL.ProductDAO; // Add this import
import javafx.collections.ObservableList;

import java.sql.SQLException;
import java.util.List;

public class ProductManager {
    private IProductDataAccess productDataAccess;
    
    public ProductManager() throws BelmanBLLException {
        try {
            this.productDataAccess = new ProductDAO();
        } catch (Exception e) {
            throw new BelmanBLLException("Failed to initialize ProductDataAccess.", e);
        }
    }

    public List<Product> getAvailableProducts() throws BelmanBLLException {
        try {
            return productDataAccess.getAllProducts();
        } catch (SQLException e) {
            throw new BelmanBLLException("Failed to retrieve available products.", e);
        }
    }

    public ObservableList<Product> getProductsForOrder(String orderNumber) throws BelmanBLLException {
        try {
            return productDataAccess.getProductsForOrder(orderNumber);
        } catch (SQLException e) {
            throw new BelmanBLLException("Failed to retrieve products for order: " + orderNumber, e);
        }
    }
}