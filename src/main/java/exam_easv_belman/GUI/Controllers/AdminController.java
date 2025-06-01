package exam_easv_belman.GUI.Controllers;

import exam_easv_belman.BE.Role;
import exam_easv_belman.BE.User;
import exam_easv_belman.GUI.Models.UserModel;
import exam_easv_belman.GUI.util.AlertHelper;
import exam_easv_belman.GUI.util.Navigator;
import exam_easv_belman.BLL.util.SessionManager;
import exam_easv_belman.GUI.util.TimerManager;
import exam_easv_belman.GUI.util.View;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;
import javafx.stage.FileChooser;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.Random;
import java.util.ResourceBundle;
import javafx.animation.PauseTransition;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.TextField;
import javafx.util.Duration;

public class AdminController implements Initializable {
    @FXML
    public Button btnLogout;
    @FXML
    public Button btnCreateUser;
    @FXML
    public Button btnDeleteUser;
    @FXML
    public ListView<User> lstUsers;
    @FXML
    public TextField txtUsername;
    @FXML
    public TextField txtPassword;
    @FXML
    public TextField txtFirstName;
    @FXML
    public TextField txtLastName;
    @FXML
    public TextField txtEmail;
    @FXML
    public TextField txtPhone;
    @FXML
    public Label lblCurrentUser;
    @FXML
    private Button btnSignatur;
    @FXML
    private Button btnUpdateUser;

    boolean passwordChanged = false;

    @FXML
    private Button btnOperator;
    @FXML
    private Button btnQC;


    private UserModel userModel;
    private User selectedUser;
    @FXML
    private Circle objStatus;
    @FXML
    private Label lblUser;
    private TimerManager timerManager;


    public AdminController() {
        try {
            userModel = new UserModel();
        } catch (Exception e) {
            e.printStackTrace();
            //TODO alert
        }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        if(SessionManager.getInstance().getCurrentOrderNumber() == null)
        {
            btnOperator.setDisable(true);
            btnQC.setDisable(true);
        }
        populateUserList();
        //lblCurrentUser.setText(SessionManager.getInstance().getCurrentUser().getUsername());
        btnUpdateUser.setVisible(false);
        if (lstUsers.getItems() != null) {
            lstUsers.getSelectionModel().select(0);
            User user = lstUsers.getSelectionModel().getSelectedItem();
            setUserInfo(user);
        }
        lstUsers.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            setUserInfo(newValue);
        });

        lstUsers.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                selectedUser = newSelection;
                // Show or hide the signature button based on role
                btnSignatur.setVisible(selectedUser.getRole() == Role.QC);
            } else {
                btnSignatur.setVisible(false);
            }
        });

        btnSignatur.setVisible(false);

        txtUsername.textProperty().addListener((observable, oldValue, newValue) -> {
            btnUpdateUser.setVisible(true);
        });

        txtPassword.textProperty().addListener((observable, oldValue, newValue) -> {
            btnUpdateUser.setVisible(true);
            passwordChanged = true;

        });

        txtFirstName.textProperty().addListener((observable, oldValue, newValue) -> {
            btnUpdateUser.setVisible(true);
        });

        txtLastName.textProperty().addListener((observable, oldValue, newValue) -> {
            btnUpdateUser.setVisible(true);
        });

        txtEmail.textProperty().addListener((observable, oldValue, newValue) -> {
            btnUpdateUser.setVisible(true);
        });

        txtPhone.textProperty().addListener((observable, oldValue, newValue) -> {
            btnUpdateUser.setVisible(true);
        });

    lblUser.setText(SessionManager.getInstance().getCurrentUser().getFirstName() + " " + SessionManager.getInstance().getCurrentUser().getLastName());
    timerManager = new TimerManager(objStatus);
    timerManager.initialize();
    }

    /**
     * logs the user out
     * @param actionEvent the event that triggered this method
     */
    @FXML
    public void handleLogout(ActionEvent actionEvent) {
        Navigator.getInstance().goTo(View.LOGIN);
        SessionManager.getInstance().logout();
    }

    /**
     * creates a new user
     * @param actionEvent the event that triggered this method
     */
    @FXML
    private void handleCreateUser(ActionEvent actionEvent) {
        Object controllerObj = Navigator.getInstance().showModal(View.USER_CREATION_MODAL);

        if (controllerObj instanceof UserCreationDialogController controller) {
            User newUser = controller.getResult();
            if (newUser != null) {
                try {
                    newUser.setQrKey(generateRandomString());
                    showCopyableQRCode(newUser.getQrKey());
                    //making a new User object to make sure parity between the persisted data and what will be displayed.
                    User user = userModel.createUser(newUser);
                    lstUsers.getItems().add(user);
                    lstUsers.getSelectionModel().select(user);
                } catch (Exception e) {
                    e.printStackTrace();
                    AlertHelper.showAlert("Error", "An error occurred while attempting to create the user.", Alert.AlertType.ERROR);
                }
            }

        }
    }

    /**
     * Sets the users information in the text fields
     * @param selectedUser
     */
    private void setUserInfo(User selectedUser) {
        if (selectedUser != null) {
            this.selectedUser = selectedUser;
            txtUsername.setText(selectedUser.getUsername());
            txtPassword.setText("*****");
            txtFirstName.setText(selectedUser.getFirstName());
            txtLastName.setText(selectedUser.getLastName());
            txtEmail.setText(selectedUser.getEmail());
            txtPhone.setText(selectedUser.getPhoneNumber());
        }
    }

    /**
     * deletes selected user from the database
     * @param actionEvent the event that triggered this method
     */
    @FXML
    public void handleDeleteUser(ActionEvent actionEvent) {
        User selectedUser = lstUsers.getSelectionModel().getSelectedItem();
        if (selectedUser == null) {

            AlertHelper.showAlert("No User Selected","You must select a user from the list before deleting.", Alert.AlertType.WARNING);
            return;
        }

        // Confirm deletion

        AlertHelper.showConfirmationAlert("Delete User", "Are you sure you want to delete" + selectedUser.getUsername(), () ->{try {
            // Delete the user using the model
            userModel.deleteUser(selectedUser);

            // Refresh the user list
            populateUserList();
        } catch (Exception e) {
            e.printStackTrace();

            // Show an error alert
            AlertHelper.showAlert("Error", "An error occurred while attempting to delete the user.", Alert.AlertType.ERROR);

        }});

    }

    /**
     * populates the user list with all users from the database
     */
    private void populateUserList() {
        ObservableList<User> users = FXCollections.observableArrayList();
        try {

            users = userModel.getAllUsers();
        } catch (Exception e) {
            e.printStackTrace();
            AlertHelper.showAlert("Error","Unable to PopulateUserList.", Alert.AlertType.ERROR);
        }

        if (users == null) {
            Label lblNoUsers = new Label("No users found");
            lstUsers.setPlaceholder(lblNoUsers);
        } else {
            users.sort(Comparator.comparing(user -> user.getFirstName().toLowerCase()));
            lstUsers.setItems(users);
            setupCellFactory();
        }
    }

    /**
     * Sets up the cell factory for the list view.
     */
    private void setupCellFactory() {
        lstUsers.setCellFactory(param -> new ListCell<User>() {
            @Override
            protected void updateItem(User item, boolean empty) {
                super.updateItem(item, empty);

                setText(null);
                setGraphic(null);
                if (empty || item == null) {
                    return;
                }

                //TODO assign style classes in here

                Label nameLabel = new Label(item.getFirstName() + " " + item.getLastName());
                Label roleLabel;
                if (item.getRole() != null) {
                    roleLabel = new Label(item.getRole().toString().toLowerCase());
                } else {
                    roleLabel = new Label("null");
                }
                nameLabel.setStyle("-fx-font-weight: bold");

                VBox vBox = new VBox(nameLabel, roleLabel);
                setGraphic(vBox);

            }
        });
    }

    /**
     * generates a random string for the qrKey of the user
     * @return the generated string
     */
    private String generateRandomString() {
        //Lists all the possible symbols in generation:
        String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        Random random = new Random();
        //Randomly generates a length of the code between 15 and 45 characters.
        int length = random.nextInt(30) + 15;
        StringBuilder randomString = new StringBuilder();

        //Builds the string:
        for (int i = 0; i < length; i++) {
            int index = random.nextInt(characters.length());
            randomString.append(characters.charAt(index));
        }
        // Modifies the string to give the generated string length + separator before the generated string.
        // Generally used for debugging if any problems with the tickets should occur.
        return length + "USER" + randomString.toString();

    }

    /**
     * opens up a dialog with the qrKey of the user
     * @param qrKey the qrKey of the user
     */
    private void showCopyableQRCode(String qrKey) {
        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("QR Code");
        dialog.setHeaderText("User's unique QR ID (Click to copy)");

        // Create a copyable text field
        TextField textField = new TextField(qrKey);
        textField.setEditable(false);
        textField.setPrefWidth(300);

        // Create a copy button
        Button copyButton = new Button("Copy to Clipboard");
        copyButton.setOnAction(e -> {
            textField.selectAll();
            textField.copy();
            // Show a brief confirmation
            copyButton.setText("Copied!");
            PauseTransition pause = new PauseTransition(Duration.seconds(2));
            pause.setOnFinished(event -> copyButton.setText("Copy to Clipboard"));
            pause.play();
        });

        // Create layout for dialog
        VBox content = new VBox(10); // 10 is spacing between elements
        content.getChildren().addAll(textField, copyButton);
        content.setPadding(new Insets(10));

        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.OK);

        dialog.showAndWait();
    }

    /**
     * Sets the signature for the selected user.
     * @param actionEvent the event that triggered this method
     * @throws IOException if an error occurs while setting the signature
     */
    public void handleSetSignature(ActionEvent actionEvent) throws IOException {
        if (selectedUser != null && selectedUser.getRole() == Role.QC) {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Select Signature File");

            fileChooser.getExtensionFilters().addAll(
                    new FileChooser.ExtensionFilter("PNG Files", "*.png")
            );

            File selectedFile = fileChooser.showOpenDialog(btnSignatur.getScene().getWindow());
            if (selectedFile != null) {
                Path source = selectedFile.toPath();

                String projectDir = System.getProperty("user.dir");
                Path destination = Path.of(projectDir, "src/main/resources/Images/Signatur", selectedFile.getName());

                Files.createDirectories(destination.getParent());

                Files.copy(source, destination, java.nio.file.StandardCopyOption.REPLACE_EXISTING);

                String signaturPath = "src/main/resources/Images/Signatur/" + selectedFile.getName();
                selectedUser.setSignaturePath(signaturPath);

                try {
                    UserModel userModel = new UserModel();
                    userModel.attachSignature(selectedUser);

                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle("Success");
                    alert.setHeaderText(null);
                    alert.setContentText("Signature has been successfully set!");
                    alert.showAndWait();
                } catch (Exception e) {
                    e.printStackTrace();
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Error");
                    alert.setHeaderText(null);
                    alert.setContentText("Failed to save the signature: " + e.getMessage());
                    alert.showAndWait();
                }
            }
        }
    }

    /**
     * sends the user to the orderView
     * @param actionEvent the event that triggered this method
     */
    public void handleOrder(ActionEvent actionEvent) {
        try {
            Navigator.getInstance().goTo(View.ORDER);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * updates the user in the database
     * @param actionEvent the event that triggered this method
     */
    @FXML
    private void handleUpdateUser(ActionEvent actionEvent) {
        AlertHelper.showConfirmationAlert("Update User", "Are you sure you want to update" + selectedUser.getUsername(), () ->{
            try {
            selectedUser.setUsername(txtUsername.getText());
            if(passwordChanged) {
                selectedUser.setPassword(txtPassword.getText());
            }
            selectedUser.setFirstName(txtFirstName.getText());
            selectedUser.setLastName(txtLastName.getText());
            selectedUser.setEmail(txtEmail.getText());
            selectedUser.setPhoneNumber(txtPhone.getText());

            userModel.updateUser(selectedUser, passwordChanged);
        } catch(Exception e) {
            e.printStackTrace();
            AlertHelper.showAlert("Error", "An error occurred while attempting to update the user.", Alert.AlertType.ERROR);
        }
        });
    }

    /**
     * sends the user to the photoDocView
     * @param actionEvent the event that triggered this method
     */
    @FXML
    private void handleOperator(ActionEvent actionEvent) {
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

    /**
     * sends the user to the QCView
     * @param actionEvent the event that triggered this method
     */
    @FXML
    public void handleQC(ActionEvent actionEvent) {
        timerManager.cleanup();
        try {
            Navigator.getInstance().setRoot(View.QCView, controller -> {
                if (controller instanceof QCController) {
                    try {
                        ((QCController) controller).setOrderNumber(SessionManager.getInstance().getCurrentOrderNumber());
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            AlertHelper.showAlert("Error", "Failed to load QCView", Alert.AlertType.ERROR);

        }

    }
}

