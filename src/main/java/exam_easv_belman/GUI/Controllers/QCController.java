package exam_easv_belman.GUI.Controllers;

import exam_easv_belman.BE.Photo;
import exam_easv_belman.BE.Product;
import exam_easv_belman.BE.Role;
import exam_easv_belman.BE.User;
import exam_easv_belman.GUI.Models.PhotoModel;
import exam_easv_belman.GUI.Models.ProductModel;
import exam_easv_belman.GUI.util.Navigator;
import exam_easv_belman.BLL.util.SessionManager;
import exam_easv_belman.GUI.util.TimerManager;
import exam_easv_belman.GUI.util.View;
import exam_easv_belman.GUI.util.AlertHelper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.SQLException;
import java.util.Objects;
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

    private ObservableList<Photo> orderOfPhotos;
    private final String[] tagOrder = {"Front", "Back", "Left", "Right", "Top", "Additional"};
    private int tagIndex;

    private ObservableList<Photo> imagesFromDatabase;

    @FXML
    private StackPane photoGridContainer;
    private boolean isProduct;

    private static final int MAX_PHOTOS = 6;
    private PhotoModel photoModel;
    @FXML
    private MenuButton btnProduct;
    private ProductModel productModel;
    private ObservableList<Photo> additionalImagesFromDatabase;
    @FXML
    private Circle objStatus;
    @FXML
    private Label lblUser;
    private TimerManager timerManager;


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
            additionalImagesFromDatabase.clear();
            for(Photo photo : imagesFromDatabase)
            {
                if(java.util.Objects.equals(photo.getTag(), "Additional"))
                {
                    additionalImagesFromDatabase.add(photo);
                }
            }
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
        orderOfPhotos = FXCollections.observableArrayList();
        additionalImagesFromDatabase = FXCollections.observableArrayList();
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

        lblUser.setText(currentUser.getFirstName() + " " + currentUser.getLastName());
        timerManager = new TimerManager(objStatus);
        timerManager.initialize();
    }


    public void handleReturn(ActionEvent actionEvent) {
        String orderNumber = SessionManager.getInstance().getCurrentOrderNumber();
        if (orderNumber == null || orderNumber.isEmpty()) {
            AlertHelper.showAlert("Error", "No order number available", Alert.AlertType.ERROR);
            return;
        }
        timerManager.cleanup();

        try {
            Navigator.getInstance().goTo(View.ORDER);
        } catch (Exception e) {
            e.printStackTrace();
            AlertHelper.showAlert("Error", "Failed to load OrderView", Alert.AlertType.ERROR);
        }
    }
    private Node fillFirstPhotoGrid(int pageIndex) {

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


        int column = 0;
        int row = 0;

        gridPhoto.widthProperty().addListener((obs, oldVal, newVal) -> updateImageSizes());
        gridPhoto.heightProperty().addListener((obs, oldVal, newVal) -> updateImageSizes());
        tagIndex = 0;

        for (int i = 0; i < 5; i++) {
            Photo photo = getPhotoWithTag(tagIndex);
            StackPane imageContainer = new StackPane();
            imageContainer.setAlignment(Pos.CENTER);
            ImageView imageView = new ImageView();
            if(photo == null)
            {
                gridPhoto.add(imageContainer, column, row);
            }
            else {
                try {
                    if (Files.exists(Path.of(photo.getFilepath()))) {
                        Image image = new Image(new File(photo.getFilepath()).toURI().toString());
                        imageView.setImage(image);

                        imageView.fitWidthProperty().bind(gridPhoto.widthProperty().divide(2.2));
                        imageView.fitHeightProperty().bind(gridPhoto.heightProperty().divide(3.2));
                        imageView.setPreserveRatio(true);

                        GridPane.setMargin(imageView, new Insets(5));

                        imageContainer.getChildren().add(imageView);
                    } else {
                        Label tempLabel = new Label("Image not found");
                        tempLabel.getStylesheets().add("/css/general.css");
                        tempLabel.getStyleClass().add("label-image");

                        imageContainer.getChildren().add(tempLabel);
                    }
                    // Add the container with the tags and image to the grid
                    GridPane.setHalignment(imageContainer, HPos.CENTER);
                    GridPane.setValignment(imageContainer, VPos.CENTER);
                    GridPane.setFillHeight(imageContainer, true);
                    GridPane.setFillWidth(imageContainer, true);

                    VBox labelContainer = new VBox();
                    labelContainer.setAlignment(Pos.BOTTOM_CENTER);
                    labelContainer.setPadding(new Insets(0, 0, 6, 0));

                    Label tagLabel = new Label(tagOrder[tagIndex]);
                    tagLabel.getStylesheets().add("/css/photoDoc.css");
                    tagLabel.getStyleClass().add("photo-tag-label");
                    tagLabel.setStyle(getTagStyleBasedOnVerification(photo));

                    tagLabel.setAlignment(Pos.CENTER);
                    tagLabel.setMinWidth(100);
                    tagLabel.prefWidthProperty().bind(imageView.fitWidthProperty().divide(2.2));

                    //Approve or deny logic implementation
                    ContextMenu contextMenu = new ContextMenu();
                    MenuItem approve = new MenuItem("Approve");
                    approve.setOnAction(event -> {
                        try {
                            photoModel.changeVeirfyState(photo, 2);
                            photo.setVerifyStatus(2);
                            tagLabel.setStyle(getTagStyleBasedOnVerification(photo));

                        } catch (SQLException e) {
                            throw new RuntimeException(e);
                        }
                    });
                    MenuItem deny = new MenuItem("Deny");
                    deny.setOnAction(event -> {
                        try {
                            photoModel.changeVeirfyState(photo,3);
                            photo.setVerifyStatus(3);
                            tagLabel.setStyle(getTagStyleBasedOnVerification(photo));
                            openCommentView(photo);

                        } catch (SQLException | IOException e) {
                            throw new RuntimeException(e);
                        }
                    });

                    contextMenu.getItems().addAll(approve, deny);

                    labelContainer.setOnMouseClicked(event -> {
                        if(event.getButton() == MouseButton.PRIMARY) {
                            handleImageClick(photo);
                        }
                        else if (event.getButton() == MouseButton.SECONDARY) {
                            contextMenu.show(labelContainer, event.getScreenX(), event.getScreenY());
                        }
                    });

                    labelContainer.getChildren().add(tagLabel);

                    imageContainer.getChildren().add(labelContainer);
                    StackPane.setAlignment(tagLabel, Pos.BOTTOM_CENTER);


                    if (i == 4 && !orderOfPhotos.contains(photo)) {
                        orderOfPhotos.add(photo);
                        orderOfPhotos.addAll(additionalImagesFromDatabase);
                    }
                    if (!orderOfPhotos.contains(photo)) {
                        orderOfPhotos.add(photo);
                    }


                    gridPhoto.add(imageContainer, column, row);

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
                tagIndex++;
                column++;
                if (column > 1) {
                    column = 0;
                    row++;
                }

        }
        return photoGridContainer;
    }

    private void openCommentView(Photo photo) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/views/CommentView.fxml"));
        Parent root = fxmlLoader.load();
        CommentController controller = fxmlLoader.getController();
        controller.setPhoto(photo);

        Stage stage = new Stage();
        stage.setTitle("Add comment");
        stage.setScene(new Scene(root));
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.showAndWait();
    }

    private Photo getPhotoWithTag(int tagIndex) {
        for(Photo photo : imagesFromDatabase)
        {
            if(Objects.equals(photo.getTag(), tagOrder[tagIndex]))
            {
                return photo;

            }
        }
        return null;
    }

    private Node fillAdditionalPhotoGrid(int pageIndex) {

        gridPhoto.getChildren().clear();
        int startIndex = (pageIndex-1) * MAX_PHOTOS;
        int endIndex = Math.min(startIndex + MAX_PHOTOS, additionalImagesFromDatabase.size());
        int column = 0;
        int row = 0;

        gridPhoto.widthProperty().addListener((obs, oldVal, newVal) -> updateImageSizes());
        gridPhoto.heightProperty().addListener((obs, oldVal, newVal) -> updateImageSizes());

        for (int i = startIndex; i < endIndex; i++) {
            Photo photo = additionalImagesFromDatabase.get(i);

            StackPane imageContainer = new StackPane();
            // imageContainer.setSpacing(5); // Space between tags and image
            imageContainer.setAlignment(Pos.CENTER);
            ImageView imageView = new ImageView();

            try {
                if (Files.exists(Path.of(photo.getFilepath()))) {
                    Image image = new Image(new File(photo.getFilepath()).toURI().toString());
                    imageView.setImage(image);

                    imageView.fitWidthProperty().bind(gridPhoto.widthProperty().divide(2.2));
                    imageView.fitHeightProperty().bind(gridPhoto.heightProperty().divide(3.2));
                    imageView.setPreserveRatio(true);

                    GridPane.setMargin(imageView, new Insets(5));

                    imageContainer.getChildren().add(imageView);
                } else {
                    Label tempLabel = new Label("Image not found");
                    tempLabel.getStylesheets().add("/css/general.css");
                    tempLabel.getStyleClass().add("label-image");

                    imageContainer.getChildren().add(tempLabel);
                }
                // Add the container with the tags and image to the grid
                GridPane.setHalignment(imageContainer, HPos.CENTER);
                GridPane.setValignment(imageContainer, VPos.CENTER);
                GridPane.setFillHeight(imageContainer, true);
                GridPane.setFillWidth(imageContainer, true);

                VBox labelContainer = new VBox();
                labelContainer.setAlignment(Pos.BOTTOM_CENTER);
                labelContainer.setPadding(new Insets(0,0,6,0));

                Label tagLabel = new Label("Additional");
                tagLabel.getStylesheets().add("/css/photoDoc.css");
                tagLabel.getStyleClass().add("photo-tag-label");
                tagLabel.setStyle(getTagStyleBasedOnVerification(photo));

                tagLabel.setAlignment(Pos.CENTER);
                tagLabel.setMinWidth(100);
                tagLabel.prefWidthProperty().bind(imageView.fitWidthProperty().divide(2.2));

                //Approve or deny logic implementation
                ContextMenu contextMenu = new ContextMenu();
                MenuItem approve = new MenuItem("Approve");
                approve.setOnAction(event -> {
                    try {
                        photoModel.changeVeirfyState(photo, 2);
                        photo.setVerifyStatus(2);
                        tagLabel.setStyle(getTagStyleBasedOnVerification(photo));

                    } catch (SQLException e) {
                        throw new RuntimeException(e);
                    }
                });
                MenuItem deny = new MenuItem("Deny");
                deny.setOnAction(event -> {
                    try {
                        photoModel.changeVeirfyState(photo,3);
                        photo.setVerifyStatus(3);
                        tagLabel.setStyle(getTagStyleBasedOnVerification(photo));
                        openCommentView(photo);

                    } catch (SQLException | IOException e) {
                        throw new RuntimeException(e);
                    }
                });

                contextMenu.getItems().addAll(approve, deny);

                labelContainer.setOnMouseClicked(event -> {
                    if(event.getButton() == MouseButton.PRIMARY) {
                        handleImageClick(photo);
                    }
                    else if (event.getButton() == MouseButton.SECONDARY) {
                        contextMenu.show(labelContainer, event.getScreenX(), event.getScreenY());
                    }
                });

                labelContainer.getChildren().add(tagLabel);

                imageContainer.getChildren().add(labelContainer);
                StackPane.setAlignment(tagLabel, Pos.BOTTOM_CENTER);

                gridPhoto.add(imageContainer, column, row);

            } catch (Exception e) {
                e.printStackTrace();
            }
            tagIndex++;
            column++;
            if (column > 1) {
                column = 0;
                row++;
            }
        }
        return photoGridContainer;
    }

    private Node fillPhotoGrid(int pageIndex) {
        gridPhoto.getChildren().clear();

        if(pageIndex == 0)
        {
            return fillFirstPhotoGrid(pageIndex);
        }
        else
        {
            return fillAdditionalPhotoGrid(pageIndex);
        }
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
        timerManager.cleanup();
        try {
            Navigator.getInstance().setRoot(View.IMG_VIEW, controller -> {
                System.out.println(controller);
                if (controller instanceof ImageController) {
                    ((ImageController) controller).setImage(photo);
                    ((ImageController) controller).setPhotoOrder(orderOfPhotos);
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
        timerManager.cleanup();
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

    public void handleOperator(ActionEvent actionEvent) {
        timerManager.cleanup();
        try {
            Navigator.getInstance().setRoot(View.PHOTO_DOC, controller -> {
                if (controller instanceof PhotoDocController) {
                    try {
                        ((PhotoDocController) controller).setOrderNumber(SessionManager.getInstance().getCurrentOrderNumber());
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            AlertHelper.showAlert("Error", "Failed to load PhotoDocView", Alert.AlertType.ERROR);

        }
    }

    public void handleUserManagement(ActionEvent actionEvent) {
        timerManager.cleanup();
        try {
            Navigator.getInstance().goTo(View.ADMIN);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String getTagStyleBasedOnVerification(Photo photo) {
            int verifyState = photo.getVerifyStatus();
            if (verifyState == 2) {
                return "-fx-background-color: rgba(26,110,26,0.75)";
            }

            else if (verifyState == 3) {
                return "-fx-background-color: rgba(159,5,5,0.75)";
            }

            return "-fx-background-color: rgba(0, 0, 0, 0.7);";
    }

}
