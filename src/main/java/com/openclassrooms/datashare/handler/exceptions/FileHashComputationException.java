package com.openclassrooms.datashare.handler.exceptions;

public class FileHashComputationException extends RuntimeException {
    public FileHashComputationException(String message) {
        super(message);
    }

    public FileHashComputationException(String message, Throwable cause) {
        super(message, cause);
    }
}
