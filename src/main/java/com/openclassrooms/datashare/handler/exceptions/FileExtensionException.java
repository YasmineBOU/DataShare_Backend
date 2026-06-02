package com.openclassrooms.datashare.handler.exceptions;

public class FileExtensionException extends RuntimeException {
    public FileExtensionException(String message) {
        super(message);
    }

    public FileExtensionException(String message, Throwable cause) {
        super(message, cause);
    }

}
