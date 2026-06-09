package com.openclassrooms.datashare.handler.exceptions;

/**
 * Exception thrown when the hash of a file does not match the expected hash.
 * This exception is used to indicate that the file content has been altered or
 * corrupted,
 * and it cannot be trusted for further operations.
 *
 * @see RuntimeException
 */
public class FileHashMismatchException extends RuntimeException {
    /**
     * Constructs a new FileHashMismatchException with the specified error message.
     *
     * @param message The detail message explaining the cause of the exception.
     */
    public FileHashMismatchException(String message) {
        super(message);
    }

    /**
     * Constructs a new FileHashMismatchException with the specified error message
     * and cause.
     *
     * @param message The detail message explaining the cause of the exception.
     * @param cause   The underlying cause of the exception.
     */
    public FileHashMismatchException(String message, Throwable cause) {
        super(message, cause);
    }
}
