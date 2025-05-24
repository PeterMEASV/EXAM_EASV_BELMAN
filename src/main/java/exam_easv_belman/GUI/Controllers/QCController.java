package exam_easv_belman.GUI.Controllers;

import exam_easv_belman.BE.Photo;
import exam_easv_belman.BE.Product;
import exam_easv_belman.BE.Role;
import exam_easv_belman.BE.User;
import exam_easv_belman.GUI.Models.PhotoModel;
import exam_easv_belman.GUI.Models.ProductModel;
import exam_easv_belman.GUI.Navigator;
import exam_easv_belman.GUI.SessionManager;
import exam_easv_belman.GUI.View;
import exam_easv_belman.GUI.util.AlertHelper;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Text;

import java.io.File;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.SQLException;
import java.util.ResourceBundle;

public class QCController implements Initializable {

    public MenuButton DRPDown;
    @FXML
    private Text txtOrderNumber;
    @FXML
    private Button btnPrev;
    @FXML
    private Pagination pagination;
    @FXML
    private GridPane gridPhoto;

    private ObservableList<Photo> imagesFromDatabase;

    @FXML
    private StackPane photoGridContainer;
    private boolean isProduct;

    private static final int MAX_PHOTOS = 6;
    private PhotoModel photoModel;
    @FXML
    private MenuButton btnProduct;
    private ProductModel productModel;


    public void setOrderNumber(String orderNumber) throws Exception {
        SessionManager.getInstance().setCurrentOrderNumber(orderNumber);
        txtOrderNumber.setText(orderNumber + "-");
        isProduct = SessionManager.getInstance().getIsProduct();
        if(isProduct)
        {
            String productNumber = SessionManager.getInstance().getCurrentProductNumber();
            String productIdentifier = productNumber.substring(productNumber.lastIndexOf("-")+1);
            btnProduct.setText(productIdentifier);
            imagesFromDatabase = photoModel.getImagesForProduct(SessionManager.getInstance().getCurrentProductNumber());
            int pageCount = (int) Math.ceil((double) imagesFromDatabase.size() / MAX_PHOTOS);
            pagination.setPageCount(pageCount);
            pagination.setPageFactory(this::fillPhotoGrid);
        }
        else
        {
            int pageCount = 1;
            pagination.setPageCount(pageCount);
            pagination.setPageFactory(this::fillPhotoGrid);
        }
    populateMenu();
    }

    //TODO remember to use product number properly.

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        try {
            productModel = new ProductModel();
        } catch (Exception e) {
            AlertHelper.showAlert("Error", "Failed to load QCView", Alert.AlertType.ERROR);
            throw new RuntimeException(e);
        }

        photoModel = new PhotoModel();
    Image img = new Image(getClass().getResourceAsStream("/images/icon-log.png"));
    ImageView imgView = new ImageView(img);
    btnPrev.setGraphic(imgView);

        User currentUser = SessionManager.getInstance().getCurrentUser();
        if (currentUser != null && currentUser.getRole() == Role.ADMIN) {
            DRPDown.setVisible(true);
        } else {
            DRPDown.setVisible(false);
        }
    }


    public void handleReturn(ActionEvent actionEvent) {
        String orderNumber = SessionManager.getInstance().getCurrentOrderNumber();
        if (orderNumber == null || orderNumber.isEmpty()) {
            AlertHelper.showAlert("Error", "No order number available", Alert.AlertType.ERROR);
            return;
        }

        try {
            Navigator.getInstance().goTo(View.ORDER);
        } catch (Exception e) {
            e.printStackTrace();
            AlertHelper.showAlert("Error", "Failed to load OrderView", Alert.AlertType.ERROR);
        }
    }
    private Node fillPhotoGrid(int pageIndex) {
        gridPhoto.getChildren().clear();
        if (!isProduct) {
            Label noImagesLabel = new Label("Switch to a product to see images");
            noImagesLabel.getStylesheets().add("/css/general.css");
            noImagesLabel.getStyleClass().add("label-image");
            noImagesLabel.setStyle("-fx-font-size: 18px; -fx-text-fill: #666666;");

            // Center the label in the GridPane
            GridPane.setHalignment(noImagesLabel, HPos.CENTER);
            GridPane.setValignment(noImagesLabel, VPos.CENTER);
            GridPane.setColumnSpan(noImagesLabel, 2);
            GridPane.setRowSpan(noImagesLabel, 3);

            gridPhoto.add(noImagesLabel, 0, 0);
            return photoGridContainer;
        }
        if(imagesFromDatabase.size() == 0)
        {
            Label noImagesLabel = new Label("This product has no images yet");
            noImagesLabel.getStylesheets().add("/css/general.css");
            noImagesLabel.getStyleClass().add("label-image");
            noImagesLabel.setStyle("-fx-font-size: 18px; -fx-text-fill: #666666;");

            // Center the label in the GridPane
            GridPane.setHalignment(noImagesLabel, HPos.CENTER);
            GridPane.setValignment(noImagesLabel, VPos.CENTER);
            GridPane.setColumnSpan(noImagesLabel, 2);
            GridPane.setRowSpan(noImagesLabel, 3);

            gridPhoto.add(noImagesLabel, 0, 0);
            return photoGridContainer;
        }

        int startIndex = pageIndex * MAX_PHOTOS;
        int endIndex = Math.min(startIndex + MAX_PHOTOS, imagesFromDatabase.size());
        int column = 0;
        int row = 0;

        gridPhoto.widthProperty().addListener((obs, oldVal, newVal) -> updateImageSizes());
        gridPhoto.heightProperty().addListener((obs, oldVal, newVal) -> updateImageSizes());
        for (int i = startIndex; i < endIndex && i < imagesFromDatabase.size(); i++) {
            Photo photo = imagesFromDatabase.get(i);
            try {
                ImageView imageView = new ImageView();
                if (Files.exists(Path.of(photo.getFilepath()))) {
                    Image image = new Image(new File(photo.getFilepath()).toURI().toString());
                    imageView.setImage(image);

                    imageView.fitWidthProperty().bind(gridPhoto.widthProperty().divide(2.2)); // 2.2 to account for padding
                    imageView.fitHeightProperty().bind(gridPhoto.heightProperty().divide(3.2)); // 3.2 to account for padding
                    imageView.setPreserveRatio(true);

                    GridPane.setMargin(imageView, new Insets(5));

                    imageView.setOnMouseClicked(event -> handleImageClick(photo));

                    GridPane.setHalignment(imageView, HPos.CENTER);
                    GridPane.setValignment(imageView, VPos.CENTER);
                    GridPane.setFillHeight(imageView, true);
                    GridPane.setFillWidth(imageView, true);

                    gridPhoto.add(imageView, column, row);
                } else {
                    Label tempLabel = new Label("Image not found");
                    tempLabel.getStylesheets().add("/css/general.css");
                    tempLabel.getStyleClass().add("label-image");

                    GridPane.setHalignment(tempLabel, HPos.CENTER);
                    GridPane.setValignment(tempLabel, VPos.CENTER);
                    GridPane.setFillHeight(tempLabel, true);
                    GridPane.setFillWidth(tempLabel, true);

                    gridPhoto.add(tempLabel, column, row);
                }

                column++;
                if (column > 1) {
                    column = 0;
                    row++;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return photoGridContainer;
    }

    private void updateImageSizes() {
        for (Node node : gridPhoto.getChildren()) {
            if (node instanceof ImageView) {
                ImageView imageView = (ImageView) node;
                imageView.fitWidthProperty().bind(gridPhoto.widthProperty().divide(2.2));
                imageView.fitHeightProperty().bind(gridPhoto.heightProperty().divide(3.2));
            }
        }
    }

    private void handleImageClick(Photo photo) {
        try {
            Navigator.getInstance().setRoot(View.IMG_VIEW, controller -> {
                System.out.println(controller);
                if (controller instanceof ImageController) {
                    ((ImageController) controller).setImage(photo);
                }
            });
        }
        catch (Exception e) {
            e.printStackTrace();
            AlertHelper.showAlert("Error", "Failed to load PhotoDocView", Alert.AlertType.ERROR);
        }
    }

    public void handlePrepareReport(ActionEvent actionEvent) {
        String orderNumber = SessionManager.getInstance().getCurrentOrderNumber();
        if (orderNumber == null || orderNumber.isEmpty()) {
            AlertHelper.showAlert("Error", "No order number available", Alert.AlertType.ERROR);
            return;
        }

        try {
            Navigator.getInstance().goTo(View.SEND_VIEW, controller -> {
                if (controller instanceof SendViewController) {
                    try {
                        ((SendViewController) controller).setOrderNumber(orderNumber);
                    } catch (SQLException e) {
                        AlertHelper.showAlert("Error", "Failed to load SendView", Alert.AlertType.ERROR);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            AlertHelper.showAlert("Error", "Failed to load SendView", Alert.AlertType.ERROR);
        }


    }

    public void handleOrder(ActionEvent actionEvent) {
        try {
            Navigator.getInstance().goTo(View.ORDER);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void populateMenu() throws SQLException {
        btnProduct.getItems().clear();
        ObservableList<Product> productsForOrder = productModel.getProductsForOrder(SessionManager.getInstance().getCurrentOrderNumber());

        for(Product product : productsForOrder)
        {
            String productIndex = product.getProduct_number().substring(product.getProduct_number().lastIndexOf("-")+1);
            MenuItem menuItem = new MenuItem(productIndex);
            menuItem.setOnAction(event -> {
                try {
                    SessionManager.getInstance().setIsProduct(true);
                    SessionManager.getInstance().setCurrentProductNumber(product.getProduct_number());
                    setOrderNumber(SessionManager.getInstance().getCurrentOrderNumber());
                } catch (Exception e) {
                    AlertHelper.showAlert("Error", "Failed to load PhotoDocView (PopulateMenu)", Alert.AlertType.ERROR);
                    e.printStackTrace();
                }
            });
            btnProduct.getItems().add(menuItem);
        }
    }

    public void handleQC(ActionEvent actionEvent) {
        try {
            Navigator.getInstance().goTo(View.QCView);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void handleUserManagement(ActionEvent actionEvent) {
        try {
            Navigator.getInstance().goTo(View.ADMIN);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
