package com.openclassrooms.datashare.handler.exceptions;

/**
 * Exception thrown when a file has expired and cannot be accessed.
 * This exception is used to indicate that a file's expiration date has passed,
 * and it is no longer available for download or other operations.
 *
 * @see RuntimeException
 */
public class FileExpiredException extends RuntimeException {
    /**
     * Constructs a new FileExpiredException with the specified error message.
     *
     * @param message The detail message explaining the cause of the exception.
     */
    public FileExpiredException(String message) {
        super(message);
    }
}
