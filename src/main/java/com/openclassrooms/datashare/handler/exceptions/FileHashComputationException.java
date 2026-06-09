package com.openclassrooms.datashare.handler.exceptions;

/**
 * Exception thrown when an error occurs during the computation of a file's
 * hash.
 * This exception is used to indicate that the system failed to calculate the
 * hash of a file,
 * which is essential for verifying file integrity.
 *
 * @see RuntimeException
 */
public class FileHashComputationException extends RuntimeException {
    /**
     * Constructs a new FileHashComputationException with the specified error
     * message.
     *
     * @param message The detail message explaining the cause of the exception.
     */
    public FileHashComputationException(String message) {
        super(message);
    }

    /**
     * Constructs a new FileHashComputationException with the specified error
     * message and cause.
     *
     * @param message The detail message explaining the cause of the exception.
     * @param cause   The underlying cause of the exception.
     */
    public FileHashComputationException(String message, Throwable cause) {
        super(message, cause);
    }
}
