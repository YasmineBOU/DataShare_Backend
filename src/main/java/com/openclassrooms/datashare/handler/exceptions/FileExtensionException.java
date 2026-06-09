package com.openclassrooms.datashare.handler.exceptions;

/**
 * Exception thrown when a file with a forbidden extension is attempted to be
 * uploaded.
 * This exception is used to enforce security policies by preventing the upload
 * of files
 * with specific extensions (e.g., executable files).
 *
 * @see RuntimeException
 */
public class FileExtensionException extends RuntimeException {
    /**
     * Constructs a new FileExtensionException with the specified error message.
     *
     * @param message The detail message explaining the cause of the exception.
     */
    public FileExtensionException(String message) {
        super(message);
    }

    /**
     * Constructs a new FileExtensionException with the specified error message and
     * cause.
     *
     * @param message The detail message explaining the cause of the exception.
     * @param cause   The underlying cause of the exception.
     */
    public FileExtensionException(String message, Throwable cause) {
        super(message, cause);
    }

}
