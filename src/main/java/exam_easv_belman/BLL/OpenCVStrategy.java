package exam_easv_belman.BLL;

import exam_easv_belman.BLL.exceptions.BelmanBLLException;
import exam_easv_belman.BLL.exceptions.CameraNotFoundException;
import exam_easv_belman.GUI.util.AlertHelper;
import javafx.scene.control.Alert;
import javafx.scene.image.Image;
import nu.pattern.OpenCV;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.videoio.VideoCapture;
import org.opencv.videoio.Videoio;

import java.io.ByteArrayInputStream;

public class OpenCVStrategy implements PhotoStrategy {

    private VideoCapture camera;
    private int width = 1280;
    private int height = 720;

    @Override
    public void start() throws CameraNotFoundException {
        OpenCV.loadLocally();

        camera = new VideoCapture(0);
        camera.set(Videoio.CAP_PROP_FRAME_WIDTH, width);
        camera.set(Videoio.CAP_PROP_FRAME_HEIGHT, height);

        if (!camera.isOpened()) {
            throw new CameraNotFoundException();
        }
    }

    @Override
    public void stop(){
        if (camera != null) {
            camera.release();
        }
    }

    @Override
    public Image takePhoto() {
        Mat frame = new Mat();
        camera.read(frame);
        if (frame.empty()) {
            AlertHelper.showAlert("Camera error", "Error getting frames. (ERROR OCS48)", Alert.AlertType.ERROR);
            throw new BelmanBLLException("Error getting frames. (ERROR OCS48)");
        }

        MatOfByte buffer = new MatOfByte();
        Imgcodecs.imencode(".png", frame, buffer);
        return new Image(new ByteArrayInputStream(buffer.toArray()));
    }

    @Override
    public void setSize(int width, int height) {
        this.width = width;
        this.height = height;
    }

    @Override
    public Mat grabRawMat() {
        Mat frame = new Mat();
        camera.read(frame);
        return frame;
    }

    @Override
    public Image convertToImage(Mat frame) {
        MatOfByte buffer = new MatOfByte();
        Imgcodecs.imencode(".png", frame, buffer);
        return new Image(new ByteArrayInputStream(buffer.toArray()));
    }
}
