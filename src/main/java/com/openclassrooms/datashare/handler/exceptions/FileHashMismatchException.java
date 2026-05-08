package com.openclassrooms.datashare.handler.exceptions;

public class FileHashMismatchException extends RuntimeException {
    public FileHashMismatchException(String message) {
        super(message);
    }

    public FileHashMismatchException(String message, Throwable cause) {
        super(message, cause);
    }
}
