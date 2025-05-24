package exam_easv_belman.GUI.Controllers;

import exam_easv_belman.BE.Role;
import exam_easv_belman.BE.User;
import exam_easv_belman.GUI.Models.UserModel;
import exam_easv_belman.GUI.Navigator;
import exam_easv_belman.GUI.SessionManager;
import exam_easv_belman.GUI.View;
import exam_easv_belman.GUI.util.AlertHelper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
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
import javafx.scene.layout.VBox;
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
    public TextField txtSearch;
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

    private UserModel userModel;
    private User selectedUser;


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
        populateUserList();
        //lblCurrentUser.setText(SessionManager.getInstance().getCurrentUser().getUsername());

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

    }

    @FXML
    public void handleLogout(ActionEvent actionEvent) {
        Navigator.getInstance().goTo(View.LOGIN);
        SessionManager.getInstance().logout();
    }

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
                    //TODO alert
                }
            }

        }
    }

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

    @FXML
    public void handleDeleteUser(ActionEvent actionEvent) {
        User selectedUser = lstUsers.getSelectionModel().getSelectedItem();
        if (selectedUser == null) {
            //TODO Brug AlertHelper
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("No User Selected");
            alert.setHeaderText("Please select a user to delete.");
            alert.setContentText("You must select a user from the list before deleting.");
            alert.showAndWait();
            return;
        }

        // Confirm deletion
        //TODO Brug AlertHelper
        Alert confirmationAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmationAlert.setTitle("Delete User");
        confirmationAlert.setHeaderText("Are you sure you want to delete this user?");
        confirmationAlert.setContentText("User: " + selectedUser.getUsername());

        if (confirmationAlert.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            try {
                // Delete the user using the model
                userModel.deleteUser(selectedUser);

                // Refresh the user list
                populateUserList();
            } catch (Exception e) {
                e.printStackTrace();

                // Show an error alert
                //TODO Brug AlertHelper
                Alert errorAlert = new Alert(Alert.AlertType.ERROR);
                errorAlert.setTitle("Error");
                errorAlert.setHeaderText("Unable to Delete User");
                errorAlert.setContentText("An error occurred while attempting to delete the user.");
                errorAlert.showAndWait();
            }
        }

    }

    private void populateUserList() {
        ObservableList<User> users = FXCollections.observableArrayList();
        try {

            users = userModel.getAllUsers();
        } catch (Exception e) {
            e.printStackTrace();
            //TODO alert
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

                VBox vBox = new VBox(nameLabel, roleLabel);
                setGraphic(vBox);

            }
        });
    }

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

    public void handleSetSignatur(ActionEvent actionEvent) throws IOException {
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
                    userModel.attachSignatur(selectedUser);

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

    public void handleOrder(ActionEvent actionEvent) {
        try {
            Navigator.getInstance().goTo(View.ORDER);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

