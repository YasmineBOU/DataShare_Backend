package com.openclassrooms.datashare.handler.exceptions;

public class FileDeletionException extends RuntimeException {
    public FileDeletionException(String message) {
        super(message);
    }

    public FileDeletionException(String message, Throwable cause) {
        super(message, cause);
    }

}
