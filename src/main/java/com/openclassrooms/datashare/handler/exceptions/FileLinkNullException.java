package com.openclassrooms.datashare.handler.exceptions;

/**
 * Exception thrown when a file link is unexpectedly null or empty.
 * This exception is used to indicate that a file link was not generated or
 * retrieved properly,
 * preventing access to the file.
 *
 * @see RuntimeException
 */

public class FileLinkNullException extends RuntimeException {
    /**
     * Constructs a new FileLinkNullException with the specified error message.
     *
     * @param message The detail message explaining the cause of the exception.
     */
    public FileLinkNullException(String message) {
        super(message);
    }
}
