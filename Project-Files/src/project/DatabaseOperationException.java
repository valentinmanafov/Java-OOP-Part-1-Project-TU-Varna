package project;

/**
 * Custom exception class for errors that occur during database operations.
 * This allows for more specific error handling related to database actions.
 */
public class DatabaseOperationException extends Exception {
    /**
     * Constructs a new DatabaseOperationException with the specified detail message.
     * @param message The detail message.
     */
    public DatabaseOperationException(String message) {
        super(message);
    }

    /**
     * Constructs a new DatabaseOperationException with the specified detail message and cause.
     * @param message The detail message.
     * @param cause The cause of the exception.
     */
    public DatabaseOperationException(String message, Throwable cause) {
        super(message, cause);
    }
}