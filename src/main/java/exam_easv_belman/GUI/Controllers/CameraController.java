package exam_easv_belman.GUI.Controllers;

import com.google.common.base.Objects;
import exam_easv_belman.BE.User;
import exam_easv_belman.BLL.OpenCVStrategy;
import exam_easv_belman.BLL.PhotoStrategy;
import exam_easv_belman.BLL.exceptions.CameraNotFoundException;
import exam_easv_belman.GUI.Models.PhotoModel;
import exam_easv_belman.GUI.Navigator;
import exam_easv_belman.GUI.SessionManager;
import exam_easv_belman.GUI.View;
import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;

import java.awt.image.BufferedImage;
import java.net.URL;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class CameraController implements Initializable {

    @FXML
    public ImageView imgCamera;
    @FXML
    public Button btnReturn;
    @FXML
    public ImageView imgPreview2;
    @FXML
    public ImageView imgPreview1;
    @FXML
    public Button btnFinish;
    @FXML
    public StackPane cameraStackPane;
    @FXML
    public StackPane rootPane;
    @FXML
    public ImageView imgFullPreview;
    @FXML
    public VBox previewControls;
    @FXML
    public Button btnDeletePreview;
    @FXML
    public Button btnClosePreview;
    @FXML
    public Button btnCapture;

    private ScheduledExecutorService mainPreviewExecutor;
    private PhotoStrategy strategy;
    private boolean isPreview = false;

    private final ArrayDeque<Image> gallery = new ArrayDeque<>();
    private PhotoModel photoModel;
    private List<BufferedImage> imagesToSave = new ArrayList<>();
    private String productNumber;
    private int currentPreviewIndex = -1;
    private String tag;

    public CameraController() {
        photoModel = new PhotoModel();
    }

    public void setTag(String tag)
    {
        this.tag = tag;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        productNumber = SessionManager.getInstance().getCurrentProductNumber();

        Rectangle clip = new Rectangle();
        clip.widthProperty().bind(cameraStackPane.widthProperty());
        clip.heightProperty().bind(cameraStackPane.heightProperty());
        cameraStackPane.setClip(clip);

        imgCamera.setManaged(false);

        bindPreviewToRoot();
        bindCameraViewToRoot();

        imgPreview1.setOnMouseClicked(e -> openOverlayPreview(0));
        imgPreview2.setOnMouseClicked(e -> openOverlayPreview(1));

        strategy = new OpenCVStrategy();
        try {
            strategy.start();
        } catch (CameraNotFoundException e) {
            e.printStackTrace();
            //TODO alert
        } catch (Exception e) {
            e.printStackTrace();
            //TODO alert blank frames VISUALLY INFORM THE USER THAT NO CAMERA WAS FOUND USING AN IMAGE ON THE IMAGEVIEW
            // SAYING - NO CAMERA FOUND
        }
        mainPreviewExecutor = Executors.newSingleThreadScheduledExecutor();
        mainPreviewExecutor.scheduleAtFixedRate(() -> {
            try {
                Image frame = strategy.takePhoto();
                Platform.runLater(() -> {
                    imgCamera.setImage(frame);
                    adjustImage(imgCamera, cameraStackPane);
                });

            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            //30 fps
        }, 0, 33, TimeUnit.MILLISECONDS);

        btnFinish.setDisable(true);
    }


    private void bindPreviewToRoot() {
        rootPane.widthProperty().addListener((observable, oldValue, newValue) -> {
            adjustImage(imgFullPreview, rootPane);
        });
        rootPane.heightProperty().addListener((observable, oldValue, newValue) -> {
            adjustImage(imgFullPreview, rootPane);
        });
    }

    private void bindCameraViewToRoot() {
        cameraStackPane.widthProperty().addListener((observable, oldValue, newValue) -> {
            adjustImage(imgCamera, cameraStackPane);
        });
        cameraStackPane.heightProperty().addListener((observable, oldValue, newValue) -> {
            adjustImage(imgCamera, cameraStackPane);
        });
    }

    private void adjustImage(ImageView imageView, StackPane container) {
        Image frame = imageView.getImage();
        double paneHeight = container.getHeight();
        double paneWidth = container.getWidth();
        if (frame == null || paneHeight <= 0 || paneWidth <= 0) {
            return;
        }

        double scale = Math.max(paneWidth / frame.getWidth(), paneHeight / frame.getHeight());
        imageView.setPreserveRatio(true);
        imageView.setFitWidth(frame.getWidth() * scale);
        imageView.setFitHeight(frame.getHeight() * scale);
    }

    private void captureImage() {
        try {
            Image image = strategy.takePhoto();
            sendToGallery(image);
            BufferedImage bImage = SwingFXUtils.fromFXImage(image, null);
            imagesToSave.add(bImage);
            btnFinish.setDisable(false);

            if(!java.util.Objects.equals(tag, "Additional"))
            {
                openOverlayPreview(0);
            }



            //TODO 1. store image in a list, could be done through the sendToGallery method.

            //TODO 2. make a save button that calls photoDAO (not directly) to save the images.

        } catch (Exception e) {
            e.printStackTrace();
            //TODO alert
        }
    }

    private void sendToGallery(Image image) {
        if (gallery.size() == 2) {
            gallery.removeLast();
        }
        gallery.addFirst(image);

        imgPreview1.setImage((gallery.size() > 0 ? gallery.toArray(new Image[0])[0] : null));
        imgPreview2.setImage((gallery.size() > 1 ? gallery.toArray(new Image[0])[1] : null));
    }


    @FXML
    public void handleFinishCamera(ActionEvent actionEvent) {
        if (imagesToSave.isEmpty()) {
            return;
        }

        List<String> fileNames = new ArrayList<>();

        for (int i = 0; i < imagesToSave.size(); i++) {
            fileNames.add(i +"");
        }
        User currentUser = SessionManager.getInstance().getCurrentUser();
        try {
            photoModel.saveImageAndPath(imagesToSave, fileNames, currentUser, productNumber, tag);
        } catch (Exception e) {
            e.printStackTrace();
            //TODO alert
        }
        //shut down the ExecutorService and stop the use of camera
        if (mainPreviewExecutor != null && !mainPreviewExecutor.isShutdown()) {
            mainPreviewExecutor.shutdownNow();
            mainPreviewExecutor = null;
            try {
                strategy.stop();
            } catch (Exception e) {
                //TODO exception
            }
        }

        Navigator.getInstance().goTo(View.PHOTO_DOC, controller -> {
            if (controller instanceof PhotoDocController pDC) {
                try {
                    pDC.setOrderNumber(SessionManager.getInstance().getCurrentOrderNumber());
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        });
    }

    @FXML
    public void handleReturn(ActionEvent actionEvent) {
        Navigator.getInstance().setRoot(View.PHOTO_DOC, controller -> {
            try {
                ((PhotoDocController) controller).setOrderNumber(SessionManager.getInstance().getCurrentOrderNumber());
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
        //shut down the ExecutorService and stop the use of camera
        if (mainPreviewExecutor != null && !mainPreviewExecutor.isShutdown()) {
            mainPreviewExecutor.shutdownNow();
            mainPreviewExecutor = null;
            try {
                strategy.stop();
            } catch (Exception e) {
                //TODO exception
            }
        }
    }

    private void openOverlayPreview(int i) {
        isPreview = true;
        Image[] images = gallery.toArray(new Image[0]);
        if (i < images.length) {
            imgFullPreview.setImage(images[i]);
            imgFullPreview.setVisible(true);
            previewControls.setVisible(true);
            btnFinish.setVisible(false);
            btnReturn.setVisible(false);
            btnCapture.setVisible(false);
            currentPreviewIndex = i;

            adjustImage(imgFullPreview, rootPane);
        }
    }
    @FXML
    public void handleDeletePreview(ActionEvent actionEvent) {
       deletePreview();
    }

    private void updatePreviews() {
        Image[] images = gallery.toArray(new Image[0]);
        imgPreview1.setImage((images.length > 0 ? images[0] : null));
        imgPreview2.setImage((images.length > 1 ? images[1] : null));
    }

    public void deletePreview() {
        if (currentPreviewIndex < 0) {
            return;
        }
        Image[] images = gallery.toArray(new Image[0]);
        Image imageToDelete = images[currentPreviewIndex];

        gallery.remove(imageToDelete);
        if (currentPreviewIndex < imagesToSave.size()) {
            imagesToSave.remove(currentPreviewIndex);
        }
        updatePreviews();
        closePreview();
    }

    @FXML
    public void handleClosePreview(ActionEvent actionEvent) {

        if (java.util.Objects.equals(tag, "Additional")) {
            closePreview();
        }
        else
        {
            handleFinishCamera(new ActionEvent());
        }
    }

    public void closePreview() {
            isPreview = false;
            imgFullPreview.setVisible(false);
            previewControls.setVisible(false);
            btnFinish.setVisible(true);
            btnReturn.setVisible(true);
            btnCapture.setVisible(true);
            currentPreviewIndex = -1;
    }

    @FXML
    public void handleCaptureImage(ActionEvent actionEvent) {
        captureImage();
    }

    @FXML
    private void handlePressImage(MouseEvent mouseEvent) {
        if(!isPreview)
        captureImage();
    }
}
