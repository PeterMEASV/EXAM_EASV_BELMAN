package exam_easv_belman.GUI.Controllers;

import exam_easv_belman.BLL.QCReportManager;
import exam_easv_belman.BLL.exceptions.BelmanGUIException;
import exam_easv_belman.BLL.util.Gmailer;
import exam_easv_belman.BLL.util.PdfPreviewUtil;
import exam_easv_belman.GUI.Models.OrderModel;
import exam_easv_belman.GUI.util.Navigator;
import exam_easv_belman.BLL.util.SessionManager;
import exam_easv_belman.GUI.util.View;
import exam_easv_belman.GUI.util.AlertHelper;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.text.Text;

import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.security.GeneralSecurityException;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SendViewController implements Initializable {

    @FXML
    private Text txtOrderNumber;
    @FXML
    private Button btnPrev;
    @FXML
    private TextField txtEmail;
    @FXML
    private TextArea txtComment;
    private OrderModel orderModel;

    private Gmailer gMailer;
    @FXML
    private Button btnLog;

    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    public void setOrderNumber(String orderNumber) throws Exception {
        if (orderModel == null) {
            orderModel = new OrderModel();
        }
        SessionManager.getInstance().setCurrentOrderNumber(orderNumber);
        txtOrderNumber.setText(orderNumber);
        String email = orderModel.getEmailForOrder(orderNumber);
        txtEmail.setText(email);
        txtComment.setText(orderModel.getCommentForOrder(txtOrderNumber.getText()));
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        try {
            orderModel = new OrderModel();
        } catch (Exception e) {
            AlertHelper.showAlert("Error", "Failed to load SendView", Alert.AlertType.ERROR);
        }
        try {
            gMailer = new Gmailer();
        } catch (GeneralSecurityException | IOException e) {
            AlertHelper.showAlert("Error", "Failed to initialize GMailer", Alert.AlertType.ERROR);
            throw new BelmanGUIException("Failed to initialize GMailer", e);
        }
        Image img = new Image(getClass().getResourceAsStream("/images/icon-back.png"));
        ImageView imgView = new ImageView(img);
        btnPrev.setGraphic(imgView);
        img = new Image(getClass().getResourceAsStream("/images/icon-log.png"));
        imgView = new ImageView(img);
        btnLog.setGraphic(imgView);
    }

    public void handleReturn(ActionEvent actionEvent) {
        String orderNumber = SessionManager.getInstance().getCurrentOrderNumber();
        if (orderNumber == null || orderNumber.isEmpty()) {
            AlertHelper.showAlert("Error", "No order number available", Alert.AlertType.ERROR);
            return;
        }
        try {
            Navigator.getInstance().goTo(View.QCView, controller -> {
                if (controller instanceof QCController) {
                    try {
                        ((QCController) controller).setOrderNumber(orderNumber);
                    } catch (Exception e) {
                        AlertHelper.showAlert("Error", "Failed to load QCView (SendViewController.java)", Alert.AlertType.ERROR);
                        throw new BelmanGUIException("Failed to load QCView (SendViewController.java)", e);
                    }
                }
            });
        } catch (Exception e) {
            AlertHelper.showAlert("Error", "Failed to load QCView", Alert.AlertType.ERROR);
            throw new BelmanGUIException("Failed to load QCView", e);
        }

    }

    public void handlePreview(ActionEvent actionEvent) {
    try {
        // Define the file path for the PDF
        String filePath = "src/main/resources/Images/" + txtOrderNumber.getText() + "_Preview.pdf";
        File pdfFile = new File(filePath);
        Stage currentStage = (Stage) txtOrderNumber.getScene().getWindow();

        Task<Void> previewTask = new Task<>() {
            @Override
            protected Void call() throws Exception {
                // Generate the PDF using QCReportManager
                QCReportManager qcReportManager = new QCReportManager();
                qcReportManager.generateQCReportPDF(filePath, txtEmail.getText(), txtComment.getText(), currentStage);
                return null;
            }
        };

        previewTask.setOnSucceeded(event -> {
            try {
                PdfPreviewUtil.showPdfPreview(pdfFile, currentStage);
            } catch (Exception e) {
                Platform.runLater(() -> 
                    AlertHelper.showAlert("Error", "Failed to preview the PDF", Alert.AlertType.ERROR)
                );
                throw new BelmanGUIException("Failed to preview the PDF", e);
            }
        });

        previewTask.setOnFailed(event -> {
            Platform.runLater(() -> {
                AlertHelper.showAlert("Error", "Failed to preview the PDF", Alert.AlertType.ERROR);
                pdfFile.delete();
            });
        });

        executor.execute(previewTask);
    } catch (Exception e) {
        AlertHelper.showAlert("Error", "Failed generating the PDF. ERROR SVC134", Alert.AlertType.ERROR);
        throw new BelmanGUIException("Failed generating the PDF. ERROR SVC134", e);
    }
}

public void handleSend(ActionEvent actionEvent) {
    // Check if the email is valid before sending
    if (!isValidEmail(txtEmail.getText())) {
        AlertHelper.showAlert(
                "Error",
                "Please input a valid email address.",
                Alert.AlertType.ERROR
        );
        return;
    }

    // File path for the PDF to be generated
    String filePath = "src/main/resources/Images/" + txtOrderNumber.getText() + ".pdf";
    File generatedPDF = new File(filePath);

    Task<Void> sendTask = new Task<>() {
        @Override
        protected Void call() throws Exception {
            // Generate the QC Report as a PDF
            QCReportManager qcReportManager = new QCReportManager();
            qcReportManager.generateQCReportPDF(
                    filePath,
                    txtEmail.getText(),
                    txtComment.getText(),
                    (Stage) txtOrderNumber.getScene().getWindow()
            );

            orderModel.addCommentToOrder(txtComment.getText(), txtOrderNumber.getText());
            gMailer.sendMail(
                    txtOrderNumber.getText(),
                    "This email contains a quality control report as per request by the client.\nThis Quality Control report is centered around the order: "
                            + txtOrderNumber.getText(),
                    txtEmail.getText(),
                    generatedPDF
            );
            return null;
        }
    };

    sendTask.setOnSucceeded(event -> {
        Platform.runLater(() -> 
            AlertHelper.showAlert("Email sent", "Email sent successfully!", Alert.AlertType.INFORMATION)
        );
        // Delete the PDF after sending the email
        boolean deleted = generatedPDF.delete();
        System.out.println("File deleted: " + deleted);
    });

    sendTask.setOnFailed(event -> {
        Platform.runLater(() -> 
            AlertHelper.showAlert("Error", "Failed to send the email. ERROR SVC203", Alert.AlertType.ERROR)
        );
    });

    executor.execute(sendTask);
}

    private boolean isValidEmail(String email) {
        // Regex for validating the email address structure
        String regex = "^[a-zA-Z0-9._%+-]+@([a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,}$";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(email);

        // If the email does not match the structure, return false.
        if (!matcher.matches()) {
            return false;
        }

        // List of most common email domains
        String[] commonDomains = {
                "gmail.com", "yahoo.com", "hotmail.com",
                "aol.com", "msn.com", "outlook.com", "easv365.dk"
        };

        // Extract the domain from the email address
        String domain = email.substring(email.indexOf('@') + 1);

        // Check if the domain is in the list of common domains
        for (String commonDomain : commonDomains) {
            if (domain.equalsIgnoreCase(commonDomain)) {
                return true;
            }
        }

        // If the domain is not in the list, return false.
        return false;
    }



    @FXML
    private void handleLog(ActionEvent actionEvent) {
        try {
            AlertHelper.showConfirmationAlert("Log out?", "Are you sure you wish to log out?", () -> {
                Navigator.getInstance().goTo(View.LOGIN);
                SessionManager.getInstance().logout();
            });
        } catch (Exception e) {
            AlertHelper.showAlert("Error", "Failed to load LoginView", Alert.AlertType.ERROR);
            throw new BelmanGUIException("Failed to load LoginView", e);
        }
    }
}