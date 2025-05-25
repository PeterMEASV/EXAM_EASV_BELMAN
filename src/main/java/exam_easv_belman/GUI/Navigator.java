package exam_easv_belman.GUI;

import exam_easv_belman.BE.Photo;
import exam_easv_belman.GUI.Controllers.ImageController;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.IOException;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * The Navigator class is responsible for managing navigation between different views in the application.
 * It implements the Singleton design pattern to ensure only one instance is created and used throughout
 * the application lifecycle. This class initializes the application's primary stage and provides methods
 * to navigate to specific views defined by the View enum.
 */
public class Navigator {
    private static Navigator instance;
    private Stage stage;
    private Object currentController;

    private Navigator() {
    }

    public static Navigator getInstance() {
        if (instance == null) {
            instance = new Navigator();
        }
        return instance;
    }

    public void init(Stage primaryStage) {
        stage = primaryStage;
        stage.setWidth(1200);
        stage.setHeight(750);
        goTo(View.LOGIN);
        stage.show();
    }

    public void goTo(View view) {
        try {
            // Store current window dimensions
            double width = stage.getWidth();
            double height = stage.getHeight();
            
            FXMLLoader loader = new FXMLLoader(Objects.requireNonNull(Navigator.class.getResource(view.getFXML())));
            Parent root = loader.load();
            
            if (stage.getScene() == null) {
                stage.setScene(new Scene(root));
            } else {
                stage.getScene().setRoot(root);
            }

            // Restore window dimensions
            stage.setWidth(width);
            stage.setHeight(height);
            
            stage.centerOnScreen();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void goTo(View view, Consumer<Object> controllerConsumer) {
        try {
            // Store current window dimensions
            double width = stage.getWidth();
            double height = stage.getHeight();
            
            FXMLLoader loader = new FXMLLoader(Objects.requireNonNull(getClass().getResource(view.getFXML())));
            Parent root = loader.load();

            if (stage.getScene() == null) {
                stage.setScene(new Scene(root));
            } else {
                stage.getScene().setRoot(root);
            }
            
            // Restore window dimensions
            stage.setWidth(width);
            stage.setHeight(height);
            
            stage.show();
            stage.centerOnScreen();

            currentController = loader.getController();
            if (controllerConsumer != null && currentController != null) {
                controllerConsumer.accept(currentController);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setRoot(View view, Consumer<Object> controllerConsumer) {
        try {
            // Store current window dimensions
            double width = stage.getWidth();
            double height = stage.getHeight();
            
            FXMLLoader loader = new FXMLLoader(Objects.requireNonNull(Navigator.class.getResource(view.getFXML())));
            Parent root = loader.load();

            if (stage.getScene() == null) {
                stage.setScene(new Scene(root));
            } else {
                stage.getScene().setRoot(root);
            }

            // Restore window dimensions
            stage.setWidth(width);
            stage.setHeight(height);
            
            currentController = loader.getController();
            if (controllerConsumer != null && currentController != null) {
                controllerConsumer.accept(currentController);
            }

            stage.centerOnScreen();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Object showModal(View view) {
        try {
            FXMLLoader loader = new FXMLLoader(Objects.requireNonNull(Navigator.class.getResource(view.getFXML())));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle("User Creation");
            stage.initStyle(StageStyle.UNDECORATED);
            stage.setScene(new Scene(root));
            stage.showAndWait();

            return loader.getController();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}