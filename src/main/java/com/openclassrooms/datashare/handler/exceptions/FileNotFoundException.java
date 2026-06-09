package com.openclassrooms.datashare.handler.exceptions;

/**
 * Exception thrown when a file is not found in the system.
 * This exception is used to indicate that a file with the specified ID or token
 * does not exist
 * or cannot be accessed by the user.
 *
 * @see RuntimeException
 */
public class FileNotFoundException extends RuntimeException {
    /**
     * Constructs a new FileNotFoundException with the specified error message.
     *
     * @param message The detail message explaining the cause of the exception.
     */
    public FileNotFoundException(String message) {
        super(message);
    }
}
