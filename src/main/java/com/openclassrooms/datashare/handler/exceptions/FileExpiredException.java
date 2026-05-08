package com.openclassrooms.datashare.handler.exceptions;

public class FileExpiredException extends RuntimeException {
    public FileExpiredException(String message) {
        super(message);
    }
}
