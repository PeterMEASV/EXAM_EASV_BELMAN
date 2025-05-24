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
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.HPos;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;

import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.SQLException;
import java.util.Objects;
import java.io.File;
import javafx.geometry.Insets;
import javafx.scene.Node;

public class PhotoDocController {
    @FXML
    public Button btnOpenCamera;
    @FXML
    private Text txtOrderNumber; // THIS CAN ALSO HOLD THE PRODUCT NUMBER, IF USER CHOOSES SO
    @FXML
    private Button btnPrev;

    private String orderNumber;


    private boolean isProduct;
    @FXML
    private GridPane gridPhoto;

    private PhotoModel photoModel;
    @FXML
    private Button btnQC;

    private static final int MAX_PHOTOS = 6;

    private ObservableList<Photo> imagesFromDatabase;
    private ObservableList<Photo> additionalImagesFromDatabase;
    @FXML
    private Pagination pagination;
    @FXML
    private StackPane photoGridContainer;
    @FXML
    private MenuButton btnProduct;
    private ProductModel productModel;
    private final String[] tagOrder = {"Front", "Back", "Left", "Right", "Top", "Additional"};
    private int tagIndex;


    @FXML
    private void initialize() throws Exception {
        additionalImagesFromDatabase = FXCollections.observableArrayList();
        productModel = new ProductModel();
        orderNumber = SessionManager.getInstance().getCurrentOrderNumber();
        photoModel = new PhotoModel();
        Image img = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/images/icon-log.png")));
        ImageView imgView = new ImageView(img);
        btnPrev.setGraphic(imgView);

        User currentUser = SessionManager.getInstance().getCurrentUser();
        if (currentUser != null && currentUser.getRole() == Role.ADMIN) {
            btnQC.setVisible(true);
        } else {
            btnQC.setVisible(false);
        }
    }

    private void handleEmptyImage(String tag) {
        System.out.println("tag opened: " + tag);
        Navigator.getInstance().goTo(View.CAMERA, controller -> {
            if(controller instanceof CameraController)
            {
                ((CameraController) controller).setTag(tag);
            }
        });
    }

    private Photo getPhotoWithTag(int tagIndex) {
        for(Photo photo : imagesFromDatabase)
        {
            String tag = photo.getTag();
            String tagToMatch = tagOrder[tagIndex];
            if(Objects.equals(photo.getTag(), tagOrder[tagIndex]))
            {
                return photo;

            }
        }
        return null;
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
        System.out.println("handleImageClick triggered with photo: " + photo);
        Navigator.getInstance().setRoot(View.IMG_VIEW, controller -> {
            System.out.println(controller);
            if (controller instanceof ImageController) {
                ImageController imageController = (ImageController) controller;

                System.out.println("Controller is an instance of ImageController.");
                imageController.setImage(photo);  // Ensure this method works
                imageController.setPhoto(photo); // Set additional photo data
            }
        });
    }
    catch (Exception e) {
        e.printStackTrace();
        AlertHelper.showAlert("Error", "Failed to load PhotoDocView", Alert.AlertType.ERROR);
    }
    }

    public void setOrderNumber(String orderNumber) throws Exception {
        SessionManager.getInstance().setCurrentOrderNumber(orderNumber);
        txtOrderNumber.setText(orderNumber + "-");
        this.orderNumber = orderNumber;
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
            int pageCount;
            if(imagesFromDatabase.isEmpty()) {
                pageCount = 1;
            }
            else {
                int additionalPhotosCount = additionalImagesFromDatabase.size();
                if (additionalPhotosCount == 0) {
                    pageCount = 1;
                } else {
                    pageCount = 1 + (int) Math.ceil((double) additionalPhotosCount / MAX_PHOTOS);
                }
            }


            pagination.setPageCount(pageCount);
            pagination.setPageFactory(this::fillPhotoGrid);
        }
        else
        {
            btnOpenCamera.setDisable(true);
            int pageCount = 1;
            pagination.setPageCount(pageCount);
            pagination.setPageFactory(this::fillPhotoGrid);
        }
        populateMenu();

    }

    public void handleCamera(ActionEvent actionEvent) {
        Navigator.getInstance().goTo(View.CAMERA);
    }

    public void handleReturn(ActionEvent actionEvent) {
        String orderNumber = SessionManager.getInstance().getCurrentOrderNumber();
        if (orderNumber == null || orderNumber.isEmpty()) {
            AlertHelper.showAlert("Error", "No order number available", Alert.AlertType.ERROR);
            return;
        }
        try {
            Navigator.getInstance().goTo(View.ORDER, controller -> {
                if (controller instanceof SendViewController) {
                    try {
                        ((SendViewController) controller).setOrderNumber(orderNumber);
                    } catch (SQLException e) {
                        AlertHelper.showAlert("Error", "Failed to load OrderView", Alert.AlertType.ERROR);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            AlertHelper.showAlert("Error", "Failed to load OrderView", Alert.AlertType.ERROR);
        }


    }

    public void handleQC(ActionEvent actionEvent){
        try {
            Navigator.getInstance().goTo(View.QCView);
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
                    btnOpenCamera.setDisable(false);
                } catch (Exception e) {
                    AlertHelper.showAlert("Error", "Failed to load PhotoDocView (PopulateMenu)", Alert.AlertType.ERROR);
                    e.printStackTrace();
                }
            });
            btnProduct.getItems().add(menuItem);
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

            if (photo == null) {
                System.out.println("AAAA SAVENJAAAA SOMETHING SOMETHING MAMAAAAA");
                Image image = new Image(getClass().getResourceAsStream("/images/icon-addPhoto.png"));
                imageView.setImage(image);

                imageView.fitWidthProperty().bind(gridPhoto.widthProperty().divide(2.2));
                imageView.fitHeightProperty().bind(gridPhoto.heightProperty().divide(3.2));
                imageView.setPreserveRatio(true);

                GridPane.setMargin(imageView, new Insets(5));
                String tag = tagOrder[tagIndex];

                imageContainer.getChildren().add(imageView);

                // Add the container with the tags and image to the grid
                GridPane.setHalignment(imageContainer, HPos.CENTER);
                GridPane.setValignment(imageContainer, VPos.CENTER);
                GridPane.setFillHeight(imageContainer, true);
                GridPane.setFillWidth(imageContainer, true);

                VBox labelContainer = new VBox();
                labelContainer.setAlignment(Pos.BOTTOM_CENTER);
                labelContainer.setPadding(new Insets(0,0,6,0));

                Label tagLabel = new Label(tagOrder[tagIndex]);
                tagLabel.getStylesheets().add("/css/photoDoc.css");
                tagLabel.getStyleClass().add("photo-tag-label");
                tagLabel.setAlignment(Pos.CENTER);
                tagLabel.setMinWidth(100);
                tagLabel.prefWidthProperty().bind(imageView.fitWidthProperty().divide(2.2));
                labelContainer.setOnMouseClicked(event -> handleEmptyImage(tag));

                labelContainer.getChildren().add(tagLabel);

                imageContainer.getChildren().add(labelContainer);
                StackPane.setAlignment(tagLabel, Pos.BOTTOM_CENTER);

                gridPhoto.add(imageContainer, column, row);

            } else {
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

                    Label tagLabel = new Label(tagOrder[tagIndex]);
                    tagLabel.getStylesheets().add("/css/photoDoc.css");
                    tagLabel.getStyleClass().add("photo-tag-label");
                    tagLabel.setAlignment(Pos.CENTER);
                    tagLabel.setMinWidth(100);
                    tagLabel.prefWidthProperty().bind(imageView.fitWidthProperty().divide(2.2));

                    labelContainer.setOnMouseClicked(event -> handleImageClick(photo));

                    labelContainer.getChildren().add(tagLabel);

                    imageContainer.getChildren().add(labelContainer);
                    StackPane.setAlignment(tagLabel, Pos.BOTTOM_CENTER);

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
            StackPane imageContainer = new StackPane();
            imageContainer.setAlignment(Pos.CENTER);
            ImageView imageView = new ImageView();
            System.out.println("CREATING ADDITIONAL BUTTON");
            Image image = new Image(getClass().getResourceAsStream("/images/icon-addPhoto.png"));
            imageView.setImage(image);

            imageView.fitWidthProperty().bind(gridPhoto.widthProperty().divide(2.2));
            imageView.fitHeightProperty().bind(gridPhoto.heightProperty().divide(3.2));
            imageView.setPreserveRatio(true);

            GridPane.setMargin(imageView, new Insets(5));
            String tag = tagOrder[tagIndex];


            imageContainer.getChildren().add(imageView);

            // Add the container with the tags and image to the grid
            GridPane.setHalignment(imageContainer, HPos.CENTER);
            GridPane.setValignment(imageContainer, VPos.CENTER);
            GridPane.setFillHeight(imageContainer, true);
            GridPane.setFillWidth(imageContainer, true);

        VBox labelContainer = new VBox();
        labelContainer.setAlignment(Pos.BOTTOM_CENTER);
        labelContainer.setPadding(new Insets(0,0,6,0));

        Label tagLabel = new Label(tagOrder[tagIndex]);
        tagLabel.getStylesheets().add("/css/photoDoc.css");
        tagLabel.getStyleClass().add("photo-tag-label");
        tagLabel.setAlignment(Pos.CENTER);
        tagLabel.setMinWidth(100);
        tagLabel.prefWidthProperty().bind(imageView.fitWidthProperty().divide(2.2));
        labelContainer.setOnMouseClicked(event -> handleEmptyImage(tag));

        labelContainer.getChildren().add(tagLabel);
        imageContainer.getChildren().add(labelContainer);
            gridPhoto.add(imageContainer, column, row);
        return photoGridContainer;
    }

    private Node fillAdditionalPhotoGrid(int pageIndex) {

        gridPhoto.getChildren().clear();
        int startIndex = (pageIndex-1) * MAX_PHOTOS;
        int endIndex = Math.min(startIndex + MAX_PHOTOS, additionalImagesFromDatabase.size());
        int column = 0;
        int row = 0;
        int count = 0;

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
                tagLabel.setAlignment(Pos.CENTER);
                tagLabel.setMinWidth(100);
                tagLabel.prefWidthProperty().bind(imageView.fitWidthProperty().divide(2.2));
                labelContainer.setOnMouseClicked(event -> handleImageClick(photo));

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

}