package exam_easv_belman.BLL.exceptions;

public class BelmanDALException extends RuntimeException {

    /**
     * Constructs a new BelmanDALException with the specified detail message.
     *
     * @param message the detail message, which provides more context about the exception.
     */
    public BelmanDALException(String message) {
        super(message);
    }

    /**
     * Constructs a new BelmanDALException with the specified detail message and cause.
     *
     * @param message the detail message, which provides more context about the exception.
     * @param cause   the cause of the exception, which can be used to trace the origin of the issue.
     */
    public BelmanDALException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Constructs a new BelmanDALException with the specified cause.
     *
     * @param cause the cause of the exception, which can be used to trace the origin of the issue.
     */
    public BelmanDALException(Throwable cause) {
        super(cause);
    }
}

