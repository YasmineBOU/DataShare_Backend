package com.openclassrooms.datashare.handler;

import java.time.LocalDateTime;

/**
 * Class representing detailed information about an error that occurred in the
 * application.
 * This class is used to encapsulate error details such as the timestamp, error
 * message, and additional context.
 * It is typically used in exception handling to provide structured error
 * information to clients.
 */
public class ErrorDetails {
    /**
     * Timestamp indicating when the error occurred.
     */
    private LocalDateTime timestamp;

    /**
     * Error message describing the cause of the error.
     */
    private String message;

    /**
     * Additional details about the error, providing more context.
     */
    private String details;

    /**
     * Constructs a new empty ErrorDetails object.
     * This constructor is provided for frameworks or libraries that require a
     * no-argument constructor.
     */
    public ErrorDetails() {
    }

    /**
     * Constructs a new ErrorDetails object with the specified timestamp, message,
     * and details.
     *
     * @param timestamp The timestamp when the error occurred.
     * @param message   The error message describing the cause of the error.
     * @param details   Additional details about the error.
     */
    public ErrorDetails(LocalDateTime timestamp, String message, String details) {
        this.timestamp = timestamp;
        this.message = message;
        this.details = details;
    }

    /**
     * Returns the timestamp when the error occurred.
     *
     * @return The timestamp as a {@link LocalDateTime}.
     */
    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    /**
     * Sets the timestamp when the error occurred.
     *
     * @param timestamp The timestamp to set.
     */
    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    /**
     * Returns the error message describing the cause of the error.
     *
     * @return The error message.
     */
    public String getMessage() {
        return message;
    }

    /**
     * Sets the error message describing the cause of the error.
     *
     * @param message The error message to set.
     */
    public void setMessage(String message) {
        this.message = message;
    }

    /**
     * Returns additional details about the error.
     *
     * @return The additional details.
     */
    public String getDetails() {
        return details;
    }

    /**
     * Sets additional details about the error.
     *
     * @param details The additional details to set.
     */
    public void setDetails(String details) {
        this.details = details;
    }
}
