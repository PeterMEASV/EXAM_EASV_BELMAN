package exam_easv_belman.GUI.Controllers;

import exam_easv_belman.BE.Order;
import exam_easv_belman.BE.Product;
import exam_easv_belman.BE.Role;
import exam_easv_belman.BE.User;
import exam_easv_belman.BLL.OrderManager;
import exam_easv_belman.BLL.exceptions.BelmanGUIException;
import exam_easv_belman.GUI.Models.ProductModel;
import exam_easv_belman.GUI.util.Navigator;
import exam_easv_belman.BLL.util.SessionManager;
import exam_easv_belman.GUI.util.TimerManager;
import exam_easv_belman.GUI.util.View;
import exam_easv_belman.GUI.util.AlertHelper;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import javafx.event.ActionEvent;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.shape.Circle;

import java.util.*;


public class OrderController {

    public MenuButton DRPDown;
    @FXML
    private TextField OrderNumber;

    private final String ORDER_REGEX = "ORD-\\d{4}(-\\d{3})?";

    private TimerManager timerManager;

    private List<String> availableOrderNumbers;

    private OrderManager orderManager;

    private List<Product> products;
    private List<String> productNames;
    private ProductModel productModel;
    @FXML
    private Button btnSearch;
    @FXML
    private Button btnLogOut;
    @FXML
    private Circle objStatus;
    @FXML
    private Label lblUser;


    @FXML
    private void initialize() throws Exception {
        btnSearch.getStyleClass().add("util-button-invalid");
        productModel = new ProductModel();
        productNames = new ArrayList<>();
        products = productModel.getAvailableProducts();
        for(Product product : products)
        {
            String tempName = product.getProduct_number();
            productNames.add(tempName);
        }

        availableOrderNumbers = new ArrayList<>();
        try {
            orderManager = new OrderManager();
            ObservableList<Order> fetchedOrders = orderManager.getAllOrders();
            for (Order order : fetchedOrders) {
                if (order.getOrderNumber() != null && !order.getOrderNumber().isEmpty()) {
                    availableOrderNumbers.add(order.getOrderNumber());
                }
            }
        } catch (Exception e) {
            AlertHelper.showAlert("Database Error", "Could not load order numbers from the database: " + e.getMessage(), Alert.AlertType.ERROR);
            throw new BelmanGUIException("Database failed", e);
        }

        Image img = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/images/icon-search.png")));
        ImageView imgView = new ImageView(img);
        btnSearch.setGraphic(imgView);
        img = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/images/icon-log.png")));
        imgView = new ImageView(img);
        btnLogOut.setGraphic(imgView);

        User currentUser = SessionManager.getInstance().getCurrentUser();
        if (currentUser != null && currentUser.getRole() == Role.ADMIN) {
            DRPDown.setVisible(true);
        } else {
            DRPDown.setVisible(false);
        }

        OrderNumber.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.matches(ORDER_REGEX)) {
                // Apply "valid" CSS class
                btnSearch.getStyleClass().remove("util-button-invalid");
                if (!btnSearch.getStyleClass().contains("util-button-valid"))
                    btnSearch.getStyleClass().add("util-button-valid");
            } else {
                // Apply "invalid" CSS class
                btnSearch.getStyleClass().remove("util-button-valid");
                if (!btnSearch.getStyleClass().contains("util-button-invalid"))
                    btnSearch.getStyleClass().add("util-button-invalid");
            }
        });

        lblUser.setText(currentUser.getFirstName() + " " + currentUser.getLastName());
        timerManager = new TimerManager(objStatus);
        timerManager.initialize();

    }


    @FXML
    private void handleSearch(ActionEvent event) {
        String inputOrderNumber = OrderNumber.getText();

        if (inputOrderNumber.isEmpty()) {
            AlertHelper.showAlert("Error", "Please enter an order/product number", Alert.AlertType.ERROR);
            return;
        }

        if (!availableOrderNumbers.contains(inputOrderNumber) && !productNames.contains(inputOrderNumber)) {

            AlertHelper.showAlert("Error", "The order/product number entered does not exist", Alert.AlertType.ERROR);
            return;
        }

        timerManager.cleanup();

        int dashCount = inputOrderNumber.length() - inputOrderNumber.replace("-", "").length();

        if(dashCount == 1) {
            SessionManager.getInstance().setCurrentOrderNumber(inputOrderNumber);

            User currentUser = SessionManager.getInstance().getCurrentUser();

            if (currentUser != null && currentUser.getRole() == Role.QC) {
                GoToQCView(event, inputOrderNumber, false);
            } else {
                GoToPhotoDocView(event, inputOrderNumber, false);
            }
        }
        if(dashCount == 2) {
            String orderNumber = inputOrderNumber.substring(0, inputOrderNumber.lastIndexOf("-"));
            SessionManager.getInstance().setCurrentOrderNumber(orderNumber);
            User currentUser = SessionManager.getInstance().getCurrentUser();

            if (currentUser != null && currentUser.getRole() == Role.QC) {
                GoToQCView(event, inputOrderNumber,true);
            } else {
                GoToPhotoDocView(event, inputOrderNumber, true);
            }
        }

    }

    public void handleLogOut(ActionEvent actionEvent) {
        SessionManager.getInstance().logout();
        Navigator.getInstance().goTo(View.LOGIN);

    }

    /**
     * goes to PhotoDocView
     * @param event
     * @param orderNumber the order number from SessionManger
     * @param IsProduct Boolean to see if a product exists
     */
    private void GoToPhotoDocView(ActionEvent event, String orderNumber, boolean IsProduct ){
        try {
            Navigator.getInstance().setRoot(View.PHOTO_DOC, controller -> {
                System.out.println(controller);
                if (controller instanceof PhotoDocController) {
                    if(!IsProduct) {
                        SessionManager.getInstance().setIsProduct(false);
                        SessionManager.getInstance().setCurrentProductNumber(null);
                    }
                    else if(IsProduct) {
                        SessionManager.getInstance().setIsProduct(true);
                        SessionManager.getInstance().setCurrentProductNumber(orderNumber);
                    }
                        try {
                            ((PhotoDocController) controller).setOrderNumber(SessionManager.getInstance().getCurrentOrderNumber());
                        } catch (Exception e) {
                            throw new BelmanGUIException("Failed to load PhotoDocView", e);
                        }
                }
            });
        } catch (Exception e) {
            AlertHelper.showAlert("Error", "Failed to load PhotoDocView", Alert.AlertType.ERROR);
            throw new BelmanGUIException("Failed to load PhotoDocView", e);
        }

    }

    private void GoToQCView(ActionEvent event, String orderNumber, boolean IsProduct){
        try {
            Navigator.getInstance().setRoot(View.QCView, controller -> {
                if (controller instanceof QCController) {

                    if(!IsProduct) {
                        SessionManager.getInstance().setIsProduct(false);
                        SessionManager.getInstance().setCurrentProductNumber(null);
                    }
                    else if(IsProduct) {
                        SessionManager.getInstance().setIsProduct(true);
                        SessionManager.getInstance().setCurrentProductNumber(orderNumber);
                    }

                    try {
                        ((QCController) controller).setOrderNumber(SessionManager.getInstance().getCurrentOrderNumber());
                    } catch (Exception e) {
                        throw new BelmanGUIException("Failed to load QCView", e);
                    }
                }
            });
            } catch (Exception e) {
            AlertHelper.showAlert("Error", "Failed to load QCView", Alert.AlertType.ERROR);
            throw new BelmanGUIException("Failed to load QCView", e);

        }

    }

    public void handleUserManagement(ActionEvent actionEvent) {
        try {
            Navigator.getInstance().goTo(View.ADMIN);
        } catch (Exception e) {
            throw new BelmanGUIException("Failed to load UserManagementView", e);
        }
    }

    public void handleOrder(ActionEvent actionEvent) {
        try {
            Navigator.getInstance().goTo(View.ORDER);
        } catch (Exception e) {
            throw new BelmanGUIException("Failed to load OrderView", e);
        }
    }
}

