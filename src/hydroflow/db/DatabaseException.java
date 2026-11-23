package hydroflow.db;

/** thrown when a database operation fails. it is an "unchecked" exception so the
 * windows do not need try/catch everywhere. */

@SuppressWarnings("serial")
public class DatabaseException extends RuntimeException {

    public DatabaseException(String message, Throwable cause) {
        super(cause == null ? message : message + "\n\nDetails: " + cause.getMessage(), cause);
    }
}
