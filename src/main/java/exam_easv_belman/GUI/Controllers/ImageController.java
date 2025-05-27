package exam_easv_belman.GUI.Controllers;

import exam_easv_belman.BE.Photo;
import exam_easv_belman.BE.Role;
import exam_easv_belman.GUI.Models.PhotoModel;
import exam_easv_belman.GUI.Navigator;
import exam_easv_belman.GUI.SessionManager;
import exam_easv_belman.GUI.View;
import exam_easv_belman.GUI.util.AlertHelper;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.SQLException;
import java.util.Objects;

public class ImageController implements Initializable {

    private Photo photo;
    private PhotoModel photoModel;


    @FXML
    private HBox rootHBox;
    @FXML
    private ImageView imageView;
    @FXML
    private VBox sidebarVbox;

    private double sidebarSize = 0.1;
    private double buttonSize = 0.7;
    private boolean isProduct;
    @FXML
    private Button btnPrev;
    @FXML
    private Text txtOrderNumber;
    @FXML
    private Button btnLog;
    @FXML
    private Button btnDelete;
    @FXML
    private Button btnConfirm;

    @FXML
    private Button btnComment;
    @FXML
    private Button btnLeft;
    @FXML
    private Button btnRight;

    private ObservableList<Photo> orderOfPhotos;

    private int photoIndex;
    @FXML
    private Label lblInfo;

    public void setImage(Photo photo) {
        this.photo = photo;
        if (Files.exists(Path.of(photo.getFilepath()))) {
            Image image = new Image(new File(photo.getFilepath()).toURI().toString());
            imageView.setImage(image);
        }
    }


    @Override
    public void initialize(java.net.URL location, java.util.ResourceBundle resources) {
        txtOrderNumber.setText(SessionManager.getInstance().getCurrentProductNumber());
        isProduct = SessionManager.getInstance().getIsProduct();
        photoModel = new PhotoModel();
        sidebarVbox.prefWidthProperty().bind(rootHBox.widthProperty().multiply(sidebarSize));
        imageView.fitWidthProperty().bind(rootHBox.widthProperty().multiply(1-sidebarSize));
        imageView.fitHeightProperty().bind(rootHBox.heightProperty());

        //todo sæt i sin egen metode. wtf.
        btnDelete.prefWidthProperty().bind(rootHBox.heightProperty().multiply(buttonSize));
        btnDelete.prefHeightProperty().bind(rootHBox.heightProperty().multiply(buttonSize));
        btnConfirm.prefWidthProperty().bind(rootHBox.heightProperty().multiply(buttonSize));
        btnConfirm.prefHeightProperty().bind(rootHBox.heightProperty().multiply(buttonSize));
        btnComment.prefWidthProperty().bind(rootHBox.heightProperty().multiply(buttonSize));
        btnComment.prefHeightProperty().bind(rootHBox.heightProperty().multiply(buttonSize));
        btnLeft.prefWidthProperty().bind(rootHBox.heightProperty().multiply(buttonSize));
        btnLeft.prefHeightProperty().bind(rootHBox.heightProperty().multiply(buttonSize));
        btnRight.prefWidthProperty().bind(rootHBox.heightProperty().multiply(buttonSize));
        btnRight.prefHeightProperty().bind(rootHBox.heightProperty().multiply(buttonSize));

        Image img = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/images/icon-log.png")));
        ImageView imgView = new ImageView(img);
        btnLog.setGraphic(imgView);
        img = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/images/icon-back.png")));
        imgView = new ImageView(img);
        btnPrev.setGraphic(imgView);

        setButtonGraphic(btnDelete, "/images/icon-trash.png");
        setButtonGraphic(btnConfirm, "/images/icon-check.png");
        setButtonGraphic(btnComment, "/images/icon-note.png");
        setButtonGraphic(btnLeft, "/images/icon-left.png");
        setButtonGraphic(btnRight, "/images/icon-right.png");
        try {
            photoModel = new PhotoModel(); // Initialize with BLL layer

        } catch (Exception e) {
            AlertHelper.showAlert("Error", "Failed to initialize PhotoModel", Alert.AlertType.ERROR);
        }

        ContextMenu contextMenu = new ContextMenu();
        MenuItem approve = new MenuItem("Approve");
        approve.setOnAction(event -> {
            try {
                photoModel.changeVeirfyState(photo, 2);
                photo.setVerifyStatus(2);

            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });
        MenuItem deny = new MenuItem("Deny");
        deny.setOnAction(event -> {
            try {
                photoModel.changeVeirfyState(photo,3);
                photo.setVerifyStatus(3);
                onHandleComment(null);

            } catch (SQLException | IOException e) {
                throw new RuntimeException(e);
            }
        });

        contextMenu.getItems().addAll(approve, deny);

        imageView.setOnMouseClicked(event -> {
            if(event.getButton() == MouseButton.SECONDARY && SessionManager.getInstance().getCurrentUser().getRole() == Role.QC)
            {
                contextMenu.show(imageView, event.getScreenX(), event.getScreenY());
            }
        });
    }



    @FXML
    private void handleReturn(ActionEvent actionEvent) {
        View path;
        if(SessionManager.getInstance().getCurrentUser().getRole() == Role.QC)
        {
            path = View.QCView;
        }
        else
        {
            path = View.PHOTO_DOC;
        }
            try {
                Navigator.getInstance().setRoot(path, controller -> {
                    if (controller instanceof PhotoDocController) {
                        try {
                                ((PhotoDocController) controller).setOrderNumber(SessionManager.getInstance().getCurrentOrderNumber());
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    }
                    else if (controller instanceof QCController) {
                        try {
                            ((QCController) controller).setOrderNumber(SessionManager.getInstance().getCurrentOrderNumber());
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

    @FXML
    private void handleLog(ActionEvent actionEvent) {
        try {
            AlertHelper.showConfirmationAlert("Log out?", "Are you sure you wish to log out?", () -> {
                Navigator.getInstance().goTo(View.LOGIN);
                SessionManager.getInstance().logout();
            });
        } catch (Exception e) {
            e.printStackTrace();
            AlertHelper.showAlert("Error", "Failed to load LoginView", Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void handleDelete(ActionEvent actionEvent) {
        AlertHelper.showConfirmationAlert("Delete this photo?", "Are you sure you wish to delete this photo?", () -> {
            try {
                photoModel.deleteImage(photo);
                handleReturn(actionEvent);
            } catch (Exception e) {
                e.printStackTrace();
                AlertHelper.showAlert("Error", "Failed to delete photo", Alert.AlertType.ERROR);
            }
        });
    }

    @FXML
    private void handleConfirm(ActionEvent actionEvent) {
        handleReturn(actionEvent);
    }

    private void setButtonGraphic(Button button, String imagePath) {
        Image img = new Image(Objects.requireNonNull(getClass().getResourceAsStream(imagePath)));
        ImageView imgView = new ImageView(img);
        button.setMaxSize(60,60);

        imgView.fitWidthProperty().bind(button.widthProperty().multiply(0.6));
        imgView.fitHeightProperty().bind(button.heightProperty().multiply(0.6));
        imgView.setPreserveRatio(true);

        button.setGraphic(imgView);
    }







    @FXML
    private void onHandleComment(ActionEvent actionEvent) throws IOException {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/views/CommentView.fxml"));
            Parent root = fxmlLoader.load();
            CommentController controller = fxmlLoader.getController();
            controller.setPhoto(photo);

            Stage stage = new Stage();
            stage.setTitle("Add comment");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.initOwner(btnComment.getScene().getWindow());
            stage.showAndWait();
        }
        catch (Exception e)
        {
            e.printStackTrace();
            AlertHelper.showAlert("Error", "Failed to load TicketManagementView", Alert.AlertType.ERROR);
        }
    }


    @FXML
    private void onHandleLeft(ActionEvent actionEvent) {
        if(photoIndex == 0)
        {
            photoIndex = orderOfPhotos.size() - 1;
        }
        else
        {
            photoIndex -= 1;
        }
        setImage(orderOfPhotos.get(photoIndex));
        lblInfo.setText(photo.getTag() + "\n" + "("+(photoIndex+1)+"/"+orderOfPhotos.size()+")");
    }

    @FXML
    private void onHandleRight(ActionEvent actionEvent) {
        if(photoIndex == orderOfPhotos.size() - 1)
        {
            photoIndex = 0;
        }
        else
        {
            photoIndex += 1;
        }
        setImage(orderOfPhotos.get(photoIndex));
        lblInfo.setText(photo.getTag() + "\n" + "("+(photoIndex+1)+"/"+orderOfPhotos.size()+")");
    }

    public void setPhotoOrder(ObservableList<Photo> orderOfPhotos) {
        this.orderOfPhotos = orderOfPhotos;
        photoIndex = orderOfPhotos.indexOf(photo);

        lblInfo.setText(photo.getTag() + "\n" + "("+(photoIndex+1)+"/"+orderOfPhotos.size()+")");
        
    }
}