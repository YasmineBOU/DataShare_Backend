package com.openclassrooms.datashare.handler.exceptions;

/**
 * Exception thrown when a file deletion operation fails.
 * This exception is used to indicate that an error occurred while attempting to
 * delete a file
 * from the storage system or the database.
 *
 * @see RuntimeException
 */
public class FileDeletionException extends RuntimeException {
    /**
     * Constructs a new FileDeletionException with the specified error message.
     *
     * @param message The detail message explaining the cause of the exception.
     */
    public FileDeletionException(String message) {
        super(message);
    }

    /**
     * Constructs a new FileDeletionException with the specified error message and
     * cause.
     *
     * @param message The detail message explaining the cause of the exception.
     * @param cause   The underlying cause of the exception.
     */
    public FileDeletionException(String message, Throwable cause) {
        super(message, cause);
    }

}
