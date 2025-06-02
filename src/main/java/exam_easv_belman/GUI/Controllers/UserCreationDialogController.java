package exam_easv_belman.GUI.Controllers;

import exam_easv_belman.BE.Role;
import exam_easv_belman.BE.User;
import exam_easv_belman.GUI.util.AlertHelper;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.net.URL;
import java.util.ResourceBundle;
import java.util.regex.Pattern;

public class UserCreationDialogController implements Initializable {


    @FXML
    private TextField txtUsername;
    @FXML
    private TextField txtPassword;
    @FXML
    private TextField txtFirstName;
    @FXML
    private TextField txtLastName;
    @FXML
    private TextField txtEmail;
    @FXML
    private TextField txtPhone;
    @FXML
    private ComboBox<Role> cmbRole;
    @FXML
    private Button btnCancel;
    @FXML
    private Button btnCreate;
    @FXML
    private Button btnCloseWindow;

    private User result;

    // Regex patterns for validation
    private static final Pattern PHONE_PATTERN = Pattern.compile("^\\d{1,8}$"); // 1 to 8 digits
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[\\w-.]+@([\\w-]+\\.)+[\\w-]{2,4}$"); // Basic email pattern


    public User getResult() {
        return result;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        cmbRole.getItems().addAll(Role.values());

    }

    @FXML
    private void handleCancel(ActionEvent actionEvent) {
        result = null;
        close();
    }

    @FXML
    private void handleCreate(ActionEvent actionEvent) {

        if(validateFields()) {
            String username = txtUsername.getText().trim();
            String password = txtPassword.getText().trim();
            Role role = cmbRole.getValue();
            String firstName = txtFirstName.getText().trim();
            String lastName = txtLastName.getText().trim();
            String email = txtEmail.getText().trim();
            String phone = txtPhone.getText().trim();

            result = new User(username, password, role, firstName, lastName, email, phone);
            close();
        }

    }


    private boolean validateFields() {
        StringBuilder errors = new StringBuilder();

        // Check for empty fields
        if (txtUsername.getText().trim().isEmpty()) {
            errors.append("- Username cannot be empty.\n");
        }
        if (txtPassword.getText().trim().isEmpty()) {
            errors.append("- Password cannot be empty.\n");
        }
        if (txtFirstName.getText().trim().isEmpty()) {
            errors.append("- First Name cannot be empty.\n");
        }
        if (txtLastName.getText().trim().isEmpty()) {
            errors.append("- Last Name cannot be empty.\n");
        }
        if (txtEmail.getText().trim().isEmpty()) {
            errors.append("- Email cannot be empty.\n");
        }
        if (txtPhone.getText().trim().isEmpty()) {
            errors.append("- Phone cannot be empty.\n");
        }
        if (cmbRole.getValue() == null) {
            errors.append("- Role must be selected.\n");
        }

        // Validate Phone Number
        String phone = txtPhone.getText().trim();
        if (!phone.isEmpty() && !PHONE_PATTERN.matcher(phone).matches()) {
            errors.append("- Phone number must be 1 to 8 digits and contain only numbers.\n");
        }

        // Validate Email
        String email = txtEmail.getText().trim();
        if (!email.isEmpty() && !EMAIL_PATTERN.matcher(email).matches()) {
            errors.append("- Email must be a valid format (e.g., user@example.com).\n");
        }

        if (errors.length() > 0) {
            AlertHelper.showAlert("Validation Error", "Please correct the following errors:\n" + errors.toString(), Alert.AlertType.ERROR);
            return false;
        }
        return true;
    }

    @FXML
    private void handleCloseWindow(ActionEvent actionEvent) {
        result = null;
        close();
    }

    private void close() {
        Stage stage = (Stage) txtUsername.getScene().getWindow();
        stage.close();
    }
}
