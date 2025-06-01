package exam_easv_belman.GUI.util;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;

import java.util.Optional;

public class AlertHelper {

    private AlertHelper() {

    }

    /**
     * An alert that shows a dialog window with the given title and content.
     * @param title - Title of the alert.
     * @param content Text shown in the alert.
     * @param alertType Type of the alert.
     */
    public static void showAlert(String title, String content, Alert.AlertType alertType) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setContentText(content);
        alert.showAndWait();
    }

    /**
     * An alert that asks confirmation from the user whether they would like to run a part of the code.
     * To use, use this format:
     * AlertHelper.showConfirmationAlert("title here", "text here", () -> { code that runs on confirm is placed here });
     *
     * If you need an example see ImageController handleLog() method.
     * @param title - Title of the alert.
     * @param content Text shown in the alert.
     * @param onConfirm The code that is run when the user clicks confirm.
     */
    public static void showConfirmationAlert(String title, String content, Runnable onConfirm) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setContentText(content);

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            onConfirm.run();
        }
    }
}
