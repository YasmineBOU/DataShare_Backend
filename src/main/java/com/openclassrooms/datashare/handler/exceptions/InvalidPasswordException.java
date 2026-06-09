package com.openclassrooms.datashare.handler.exceptions;

/**
 * Exception thrown when an invalid password is provided for a file or user.
 * This exception is used to indicate that the password provided does not match
 * the expected password,
 * preventing access to protected resources.
 *
 * @see RuntimeException
 */
public class InvalidPasswordException extends RuntimeException {
    /**
     * Constructs a new InvalidPasswordException with the specified error message.
     *
     * @param message The detail message explaining the cause of the exception.
     */
    public InvalidPasswordException(String message) {
        super(message);
    }
}
