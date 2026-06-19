package com.openclassrooms.datashare.handler.exceptions;

/**
 * Exception thrown when a user already exists and cannot be created.
 * This exception is used to indicate that a user with the specified email
 * already exists,
 * and it is not possible to create a new user with that email.
 *
 * @see RuntimeException
 */
public class UserAlreadyExistsException extends RuntimeException {
    /**
     * Constructs a new UserAlreadyExistsException with the specified error message.
     *
     * @param message The detail message explaining the cause of the exception.
     */
    public UserAlreadyExistsException(String message) {
        super(message);
    }
}
