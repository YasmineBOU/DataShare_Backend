package com.openclassrooms.datashare.handler.exceptions;

public class FileLinkGenerationException extends RuntimeException {
    public FileLinkGenerationException(String message) {
        super(message);
    }

    public FileLinkGenerationException(String message, Throwable cause) {
        super(message, cause);
    }
}
