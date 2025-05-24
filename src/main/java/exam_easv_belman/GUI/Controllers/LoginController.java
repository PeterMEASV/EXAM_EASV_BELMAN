package exam_easv_belman.GUI.Controllers;

import exam_easv_belman.BE.Role;
import exam_easv_belman.BE.User;
import exam_easv_belman.BLL.OpenCVStrategy;
import exam_easv_belman.BLL.PhotoStrategy;
import exam_easv_belman.BLL.exceptions.CameraNotFoundException;
import exam_easv_belman.GUI.Models.UserModel;
import exam_easv_belman.GUI.Navigator;
import exam_easv_belman.GUI.SessionManager;
import exam_easv_belman.GUI.View;
import exam_easv_belman.GUI.util.AlertHelper;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;

import java.net.URL;
import java.util.Objects;
import java.util.ResourceBundle;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.opencv.core.Mat;
import org.opencv.objdetect.QRCodeDetector;

public class LoginController implements Initializable {

    static {
    nu.pattern.OpenCV.loadLocally();
}

    public Button btnTogglePassword;
    public TextField txtPasswordVisible;
    private QRCodeDetector qrCodeDetector = new QRCodeDetector();

    private UserModel userModel;

    @FXML
    private TextField txtUsername;
    @FXML
    private TextField txtPassword;
    @FXML
    private Button btnLogin;
    @FXML
    private Button btnUsername;
    @FXML
    private Button btnRFID;
    @FXML
    private Button btnQR;
    @FXML
    private ImageView imgCamera;
    @FXML
    private VBox vBoxQR;
    @FXML
    private VBox vBoxRFID;
    @FXML
    private VBox vBoxUsername;

    private PhotoStrategy photoStrategy;
    private ScheduledExecutorService mainPreviewExecutor;
    @FXML
    private StackPane stackMain;
    @FXML
    private StackPane stackCam;

    public LoginController() {
        try {
            userModel = new UserModel();
        } catch (Exception e) {
            e.printStackTrace();
            //TODO alert
        }
    }


    @FXML
    private void handleLogin(ActionEvent actionEvent) {
        login();
    }


    private void login() {
        String username = txtUsername.getText().trim();
        String rawPassword = txtPassword.getText().trim();

        try {
            User user = userModel.authenticateUser(username, rawPassword);
            SessionManager.getInstance().setCurrentUser(user);

            //Navigator.getInstance().goTo(user.getRole() == Role.ADMIN ? View.ADMIN : View.ORDER);
            Navigator.getInstance().goTo(View.ORDER);

            //TODO implement some sort of visual indicator when something fails, red outline or maybe a text (not popup

        } catch (Exception e) {
            e.printStackTrace();
            //TODO exception handling
        }
    }


    @Override
    public void initialize(URL location, ResourceBundle resources) {
        enterKeyListeners();
        handleUsername(null);

        stackCam.prefWidthProperty().bind(vBoxQR.widthProperty());
        stackCam.prefHeightProperty().bind(vBoxQR.heightProperty());

        vBoxQR.maxWidthProperty().bind(stackMain.widthProperty().multiply(0.6));  // 90% of window width
        vBoxQR.maxHeightProperty().bind(stackMain.heightProperty().multiply(0.6)); // 90% of window height


        //These are crucial for the app to work. if removed, make funny animation.
        // i have no idea why. or how.
        // it just happens.
        stackCam.setMinSize(0, 0);
        vBoxQR.setMinSize(0, 0);


        imgCamera.setPreserveRatio(true);
        imgCamera.setSmooth(true);
        imgCamera.setCache(true);
        imgCamera.fitWidthProperty().bind(stackCam.widthProperty());
        imgCamera.fitHeightProperty().bind(stackCam.heightProperty());





    }

    private void enterKeyListeners() {
        txtUsername.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ENTER) {
                login();
            }
        });
        txtPassword.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ENTER) {
                login();
            }
        });
        //TODO remember this when password field is added
        /*
        txtPasswordVisible.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ENTER) {
                login();
            }
        });

         */
    }

    @FXML
    private void handleQR(ActionEvent actionEvent) {
        StackPane.setAlignment(imgCamera, Pos.CENTER);

        //enable the other buttons
        btnUsername.setDisable(false);
        btnRFID.setDisable(false);
        btnQR.setDisable(true);

        //enable vboxes
        vBoxQR.setVisible(true);
        vBoxRFID.setVisible(false);
        vBoxUsername.setVisible(false);

        photoStrategy = new OpenCVStrategy();
        imgCamera.setManaged(true);

        Rectangle clip = new Rectangle();
        clip.widthProperty().bind(stackCam.widthProperty());
        clip.heightProperty().bind(stackCam.heightProperty());
        stackCam.setClip(clip);

        try {
            photoStrategy.setSize(700,700);
            photoStrategy.start();
        } catch (Exception e)
        {
            e.printStackTrace();
            //Todo handle this :D
        }
        mainPreviewExecutor = Executors.newSingleThreadScheduledExecutor();
        mainPreviewExecutor.scheduleAtFixedRate(() -> {
            try {
                Mat rawFrame = photoStrategy.grabRawMat();
                String qrText = qrCodeDetector.detectAndDecode(rawFrame);

                if(!qrText.isEmpty()) {
                    Platform.runLater(() -> {
                        try {
                            handleQRLogin(qrText);
                        } catch (Exception e) {
                            e.printStackTrace();
                            AlertHelper.showAlert("Error", "Could not handle QRLogin (205)", Alert.AlertType.ERROR);
                        }

                    });
                }

                Image fxImage = photoStrategy.convertToImage(rawFrame);
                Platform.runLater(() -> imgCamera.setImage(fxImage));

            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            //30 fps
        }, 0, 33, TimeUnit.MILLISECONDS);

    }

    @FXML
    private void handleUsername(ActionEvent actionEvent) {

        //enable the other buttons
        btnUsername.setDisable(true);
        btnRFID.setDisable(false);
        btnQR.setDisable(false);

        //enable vboxes
        vBoxQR.setVisible(false);
        vBoxRFID.setVisible(false);
        vBoxUsername.setVisible(true);

    }

    @FXML
    private void handleRFID(ActionEvent actionEvent) {

        //enable the other buttons
        btnUsername.setDisable(false);
        btnRFID.setDisable(true);
        btnQR.setDisable(false);

        //enable vboxes
        vBoxQR.setVisible(false);
        vBoxRFID.setVisible(true);
        vBoxUsername.setVisible(false);
    }

    private void handleQRLogin(String qrCode) throws Exception {
        stopCameraPreview();
        System.out.println("M-M-MORTY YOU LITLE SHIT! MORTY! I FOUND IT! ITS " + qrCode);
        User user = userModel.authenticateUser(qrCode);
        SessionManager.getInstance().setCurrentUser(user);
        System.out.println(SessionManager.getInstance().getCurrentUser());
        Navigator.getInstance().goTo(View.ORDER);

    }
        private void stopCameraPreview() {
            try
            {
                if (mainPreviewExecutor != null && !mainPreviewExecutor.isShutdown()) {
                    mainPreviewExecutor.shutdownNow();
                }
                if (photoStrategy != null) {
                    photoStrategy.stop();
                }
            }
            catch (Exception e)
            {
                e.printStackTrace();
                AlertHelper.showAlert("Error", "could not shut down the camera.", Alert.AlertType.ERROR);
            }
        }

    public void handleTogglePassword(ActionEvent actionEvent) {
        if (txtPassword.isVisible()) {
            txtPassword.setVisible(false);
            txtPasswordVisible.setVisible(true);
            txtPassword.setMaxWidth(0);
            txtPasswordVisible.setMaxWidth(750);
            txtPasswordVisible.setText(txtPassword.getText());
            btnTogglePassword.setGraphic(new ImageView(new Image(Objects.requireNonNull(getClass().getResourceAsStream("/images/ClosedEye.png")))));
            btnTogglePassword.setStyle("-fx-background-color: transparent;");

        }
        else if(!txtPassword.isVisible()) {
            txtPassword.setVisible(true);
            txtPasswordVisible.setVisible(false);
            txtPassword.setMaxWidth(750);
            txtPasswordVisible.setMaxWidth(0);
            txtPasswordVisible.setText(txtPassword.getText());
            btnTogglePassword.setGraphic(new ImageView(new Image(Objects.requireNonNull(getClass().getResourceAsStream("/images/OpenEye.png")))));
            btnTogglePassword.setStyle("-fx-background-color: transparent;");
        }
    }
}
