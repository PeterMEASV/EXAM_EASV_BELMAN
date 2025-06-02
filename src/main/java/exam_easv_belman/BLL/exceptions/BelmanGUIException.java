package exam_easv_belman.BLL.exceptions;

public class BelmanGUIException extends RuntimeException {

    /**
     * Constructs a new BelmanGUIException with the specified detail message.
     *
     * @param message the detail message, providing more context about the exception.
     */
    public BelmanGUIException(String message) {
        super(message);
    }

    /**
     * Constructs a new BelmanGUIException with the specified detail message and cause.
     *
     * @param message the detail message, providing more context about the exception.
     * @param cause   the cause of the exception, which can be used to trace the origin of the issue.
     */
    public BelmanGUIException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Constructs a new BelmanGUIException with the specified cause.
     *
     * @param cause the cause of the exception, which can be used to trace the origin of the issue.
     */
    public BelmanGUIException(Throwable cause) {
        super(cause);
    }
}
