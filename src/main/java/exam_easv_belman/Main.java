package exam_easv_belman;

import exam_easv_belman.GUI.util.Navigator;
import javafx.application.Application;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage stage) {
        Navigator.getInstance().init(stage);
    }

    public static void main(String[] args) {
        launch();
    }

    @Override
    public void stop() throws Exception {
        super.stop();
        //implemented due to openCV keeping the JVM alive even when closing the application.
        System.exit(0);
    }
}