package com.openclassrooms.datashare.handler.exceptions;

/**
 * Exception thrown when a file exceeds the maximum allowed size.
 * This exception is used to indicate that a file being uploaded is larger than
 * the allowed limit.
 *
 * @see RuntimeException
 */
public class FileSizeExceededException extends RuntimeException {
    /**
     * Constructs a new FileSizeExceededException with the specified error message.
     *
     * @param message The detail message explaining the cause of the exception.
     */
    public FileSizeExceededException(String message) {
        super(message);
    }
}
