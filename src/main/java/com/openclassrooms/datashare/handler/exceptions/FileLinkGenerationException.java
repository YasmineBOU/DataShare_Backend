package com.openclassrooms.datashare.handler.exceptions;

/**
 * Exception thrown when a file link cannot be generated.
 * This exception is used to indicate that the system failed to create a
 * presigned URL or a download link
 * for a file, which is required for accessing the file.
 *
 * @see RuntimeException
 */
public class FileLinkGenerationException extends RuntimeException {
    /**
     * Constructs a new FileLinkGenerationException with the specified error
     * message.
     *
     * @param message The detail message explaining the cause of the exception.
     */
    public FileLinkGenerationException(String message) {
        super(message);
    }

    /**
     * Constructs a new FileLinkGenerationException with the specified error message
     * and cause.
     *
     * @param message The detail message explaining the cause of the exception.
     * @param cause   The underlying cause of the exception.
     */
    public FileLinkGenerationException(String message, Throwable cause) {
        super(message, cause);
    }
}
