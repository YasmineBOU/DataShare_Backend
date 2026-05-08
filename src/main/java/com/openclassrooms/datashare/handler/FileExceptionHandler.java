package com.openclassrooms.datashare.handler;

import com.openclassrooms.datashare.handler.exceptions.*;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import java.time.LocalDateTime;

@RestControllerAdvice
public class FileExceptionHandler {

        @ExceptionHandler(value = { FileNotFoundException.class })
        protected ResponseEntity<Object> handleFileNotFoundException(
                        FileNotFoundException fileNotFoundException,
                        WebRequest request) {
                logError(fileNotFoundException);
                return ResponseEntity
                                .status(HttpStatus.NOT_FOUND)
                                .body(getErrorDetails(fileNotFoundException, request));
        }

        @ExceptionHandler(value = { FileExpiredException.class })
        protected ResponseEntity<Object> handleFileExpiredException(
                        FileExpiredException fileExpiredException,
                        WebRequest request) {
                logError(fileExpiredException);
                return ResponseEntity
                                .status(HttpStatus.GONE)
                                .body(getErrorDetails(fileExpiredException, request));
        }

        @ExceptionHandler(value = { InvalidPasswordException.class })
        protected ResponseEntity<Object> handleInvalidPasswordException(
                        InvalidPasswordException invalidPasswordException,
                        WebRequest request) {
                logError(invalidPasswordException);
                return ResponseEntity
                                .status(HttpStatus.UNAUTHORIZED)
                                .body(getErrorDetails(invalidPasswordException, request));
        }

        @ExceptionHandler(value = { FileLinkNullException.class })
        protected ResponseEntity<Object> handleFileLinkNullException(
                        FileLinkNullException fileLinkNullException,
                        WebRequest request) {
                logError(fileLinkNullException);
                return ResponseEntity
                                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                                .body(getErrorDetails(fileLinkNullException, request));
        }

        @ExceptionHandler(value = { FileHashComputationException.class })
        protected ResponseEntity<Object> handleFileHashComputationException(
                        FileHashComputationException fileHashComputationException,
                        WebRequest request) {
                logError(fileHashComputationException);
                return ResponseEntity
                                .status(HttpStatus.BAD_REQUEST)
                                .body(getErrorDetails(fileHashComputationException, request));
        }

        @ExceptionHandler(value = { FileHashMismatchException.class })
        protected ResponseEntity<Object> handleFileHashMismatchException(
                        FileHashMismatchException fileHashMismatchException,
                        WebRequest request) {
                logError(fileHashMismatchException);
                return ResponseEntity
                                .status(HttpStatus.BAD_REQUEST)
                                .body(getErrorDetails(fileHashMismatchException, request));
        }

        @ExceptionHandler(value = { FileLinkGenerationException.class })
        protected ResponseEntity<Object> handleFileLinkGenerationException(
                        FileLinkGenerationException fileLinkGenerationException,
                        WebRequest request) {
                logError(fileLinkGenerationException);
                return ResponseEntity
                                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                                .body(getErrorDetails(fileLinkGenerationException, request));
        }

        @ExceptionHandler(value = { FileDeletionException.class })
        protected ResponseEntity<Object> handleFileDeletionException(
                        FileDeletionException fileDeletionException,
                        WebRequest request) {
                logError(fileDeletionException);
                return ResponseEntity
                                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                                .body(getErrorDetails(fileDeletionException, request));
        }

        private void logError(Exception exception) {
                org.slf4j.LoggerFactory.getLogger(getClass()).error(exception.getMessage(), exception);
        }

        private ErrorDetails getErrorDetails(Exception exception, WebRequest request) {
                return new ErrorDetails(
                                LocalDateTime.now(),
                                exception.getMessage(),
                                request.getDescription(false));
        }
}
