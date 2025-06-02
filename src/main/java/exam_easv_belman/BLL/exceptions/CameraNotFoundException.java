package exam_easv_belman.BLL.exceptions;

public class CameraNotFoundException extends Exception {
    public CameraNotFoundException() {
        super("No camera found");
    }

    public CameraNotFoundException(String message) {
        super(message);
    }

    public CameraNotFoundException(Throwable cause) {
        super(cause);
    }

    public CameraNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
