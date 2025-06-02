package exam_easv_belman.BLL.exceptions;

public class BelmanBLLException extends RuntimeException {

    /**
     * Constructs a new BelmanBLLException with the specified detail message.
     *
     * @param message the detail message, providing more context about the exception.
     */
    public BelmanBLLException(String message) {

        super(message);
    }

    /**
     * Constructs a new BelmanBLLException with the specified detail message and cause.
     *
     * @param message the detail message, providing more context about the exception.
     * @param cause   the cause of the exception, which can be used to trace the origin of the issue.
     */
    public BelmanBLLException(String message, Throwable cause) {

        super(message, cause);
    }

    /**
     * Constructs a new BelmanBLLException with the specified cause.
     *
     * @param cause the cause of the exception, which can be used to trace the origin of the issue.
     */
    public BelmanBLLException(Throwable cause) {

        super(cause);
    }
}
