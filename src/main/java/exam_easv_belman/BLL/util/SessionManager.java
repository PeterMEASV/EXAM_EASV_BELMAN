package exam_easv_belman.BLL.util;
import exam_easv_belman.BE.User;

import java.util.concurrent.atomic.AtomicReference;

//Thread-safe singleton user session manager class
public class SessionManager {
    //volatile to stop partially created objects from being used.
    private static volatile SessionManager instance;
    private User currentUser;
    private AtomicReference<String> currentOrderNumber = new AtomicReference<>();
    private boolean isProduct;
    private String currentProductNumber;

    private SessionManager() {}

/**
 * Returns the singleton instance of the SessionManager.
 * This method ensures that only one instance of SessionManager is created
 * @return The single instance of SessionManager.
 */
    public static SessionManager getInstance() {
        //if the instance is null at the time of accessing enter if statement
        if (instance == null) {
            //if multiple threads wants to access this at the same time - race conditions - the synchronized keyword
            //locks this to only allow one thread at a time.
            synchronized (SessionManager.class) {
                //if the instance is null, create a new object.
                if (instance == null) {
                    instance = new SessionManager();
                }
            }
        }
        return instance;
    }

    public void setCurrentUser(User currentUser) {
        this.currentUser = currentUser;
    }

    public User getCurrentUser() {
        return currentUser;
    }

    public void setCurrentOrderNumber(String orderNumber) {currentOrderNumber.set(orderNumber);}
    public String getCurrentOrderNumber() {return currentOrderNumber.get();}
    public void setIsProduct(boolean isProduct) {
        this.isProduct = isProduct;
    }
    public boolean getIsProduct() {
        return isProduct;
    }
    public void setCurrentProductNumber(String productNumber) {
        this.currentProductNumber = productNumber;
    }
    public String getCurrentProductNumber() {
        return currentProductNumber;
    }


    /**
     * Logs out the current user from the session.
     * This method clears the current user's information
     */
    public void logout() {
        currentUser = null;
        currentOrderNumber.set(null);

    }
}