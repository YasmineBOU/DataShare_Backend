package com.openclassrooms.datashare.handler;

import com.openclassrooms.datashare.handler.exceptions.*;

import java.time.LocalDateTime;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

/**
 * Global exception handler for file-related exceptions.
 * This class uses Spring's {@link RestControllerAdvice} to centralize the
 * handling of exceptions
 * thrown by controllers or services in the application. It converts specific
 * exceptions into
 * standardized {@link ResponseEntity} objects with appropriate HTTP status
 * codes and error details.
 *
 * <p>
 * Key functionalities include:
 * <ul>
 * <li>Handling exceptions such as {@link FileNotFoundException},
 * {@link FileExpiredException}, etc.</li>
 * <li>Returning appropriate HTTP status codes for each type of exception.</li>
 * <li>Generating structured error details using {@link ErrorDetails}.</li>
 * <li>Logging exceptions for debugging and monitoring purposes.</li>
 * </ul>
 *
 * @see RestControllerAdvice
 * @see ErrorDetails
 */
@RestControllerAdvice
public class FileExceptionHandler {

        /**
         * Handles {@link FileNotFoundException} and returns a response with HTTP status
         * 404 (Not Found).
         *
         * @param fileNotFoundException The exception to handle.
         * @param request               The web request during which the exception
         *                              occurred.
         * @return A {@link ResponseEntity} with HTTP status 404 and error details.
         */
        @ExceptionHandler(value = { FileNotFoundException.class })
        protected ResponseEntity<Object> handleFileNotFoundException(
                        FileNotFoundException fileNotFoundException,
                        WebRequest request) {
                logError(fileNotFoundException);
                return ResponseEntity
                                .status(HttpStatus.NOT_FOUND)
                                .body(getErrorDetails(fileNotFoundException, request));
        }

        /**
         * Handles {@link FileExpiredException} and returns a response with HTTP status
         * 410 (Gone).
         *
         * @param fileExpiredException The exception to handle.
         * @param request              The web request during which the exception
         *                             occurred.
         * @return A {@link ResponseEntity} with HTTP status 410 and error details.
         */
        @ExceptionHandler(value = { FileExpiredException.class })
        protected ResponseEntity<Object> handleFileExpiredException(
                        FileExpiredException fileExpiredException,
                        WebRequest request) {
                logError(fileExpiredException);
                return ResponseEntity
                                .status(HttpStatus.GONE)
                                .body(getErrorDetails(fileExpiredException, request));
        }

        /**
         * Handles {@link InvalidPasswordException} and returns a response with HTTP
         * status 401 (Unauthorized).
         *
         * @param invalidPasswordException The exception to handle.
         * @param request                  The web request during which the exception
         *                                 occurred.
         * @return A {@link ResponseEntity} with HTTP status 401 and error details.
         */
        @ExceptionHandler(value = { InvalidPasswordException.class })
        protected ResponseEntity<Object> handleInvalidPasswordException(
                        InvalidPasswordException invalidPasswordException,
                        WebRequest request) {
                logError(invalidPasswordException);
                return ResponseEntity
                                .status(HttpStatus.UNAUTHORIZED)
                                .body(getErrorDetails(invalidPasswordException, request));
        }

        /**
         * Handles {@link FileLinkNullException} and returns a response with HTTP status
         * 500 (Internal Server Error).
         *
         * @param fileLinkNullException The exception to handle.
         * @param request               The web request during which the exception
         *                              occurred.
         * @return A {@link ResponseEntity} with HTTP status 500 and error details.
         */
        @ExceptionHandler(value = { FileLinkNullException.class })
        protected ResponseEntity<Object> handleFileLinkNullException(
                        FileLinkNullException fileLinkNullException,
                        WebRequest request) {
                logError(fileLinkNullException);
                return ResponseEntity
                                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                                .body(getErrorDetails(fileLinkNullException, request));
        }

        /**
         * Handles {@link FileHashComputationException} and returns a response with HTTP
         * status 400 (Bad Request).
         *
         * @param fileHashComputationException The exception to handle.
         * @param request                      The web request during which the
         *                                     exception occurred.
         * @return A {@link ResponseEntity} with HTTP status 400 and error details.
         */
        @ExceptionHandler(value = { FileHashComputationException.class })
        protected ResponseEntity<Object> handleFileHashComputationException(
                        FileHashComputationException fileHashComputationException,
                        WebRequest request) {
                logError(fileHashComputationException);
                return ResponseEntity
                                .status(HttpStatus.BAD_REQUEST)
                                .body(getErrorDetails(fileHashComputationException, request));
        }

        /**
         * Handles {@link FileHashMismatchException} and returns a response with HTTP
         * status 400 (Bad Request).
         *
         * @param fileHashMismatchException The exception to handle.
         * @param request                   The web request during which the exception
         *                                  occurred.
         * @return A {@link ResponseEntity} with HTTP status 400 and error details.
         */
        @ExceptionHandler(value = { FileHashMismatchException.class })
        protected ResponseEntity<Object> handleFileHashMismatchException(
                        FileHashMismatchException fileHashMismatchException,
                        WebRequest request) {
                logError(fileHashMismatchException);
                return ResponseEntity
                                .status(HttpStatus.BAD_REQUEST)
                                .body(getErrorDetails(fileHashMismatchException, request));
        }

        /**
         * Handles {@link FileLinkGenerationException} and returns a response with HTTP
         * status 500 (Internal Server Error).
         *
         * @param fileLinkGenerationException The exception to handle.
         * @param request                     The web request during which the exception
         *                                    occurred.
         * @return A {@link ResponseEntity} with HTTP status 500 and error details.
         */
        @ExceptionHandler(value = { FileLinkGenerationException.class })
        protected ResponseEntity<Object> handleFileLinkGenerationException(
                        FileLinkGenerationException fileLinkGenerationException,
                        WebRequest request) {
                logError(fileLinkGenerationException);
                return ResponseEntity
                                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                                .body(getErrorDetails(fileLinkGenerationException, request));
        }

        /**
         * Handles {@link FileDeletionException} and returns a response with HTTP status
         * 500 (Internal Server Error).
         *
         * @param fileDeletionException The exception to handle.
         * @param request               The web request during which the exception
         *                              occurred.
         * @return A {@link ResponseEntity} with HTTP status 500 and error details.
         */
        @ExceptionHandler(value = { FileDeletionException.class })
        protected ResponseEntity<Object> handleFileDeletionException(
                        FileDeletionException fileDeletionException,
                        WebRequest request) {
                logError(fileDeletionException);
                return ResponseEntity
                                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                                .body(getErrorDetails(fileDeletionException, request));
        }

        /**
         * Handles {@link FileExtensionException} and returns a response with HTTP
         * status 400 (Bad Request).
         *
         * @param fileExtensionException The exception to handle.
         * @param request                The web request during which the exception
         *                               occurred.
         * @return A {@link ResponseEntity} with HTTP status 400 and error details.
         */
        @ExceptionHandler(value = { FileExtensionException.class })
        protected ResponseEntity<Object> handleFileExtensionException(
                        FileExtensionException fileExtensionException,
                        WebRequest request) {
                logError(fileExtensionException);
                return ResponseEntity
                                .status(HttpStatus.BAD_REQUEST)
                                .body(getErrorDetails(fileExtensionException, request));
        }

        /**
         * Logs the exception for debugging and monitoring purposes.
         *
         * @param exception The exception to log.
         */
        private void logError(Exception exception) {
                org.slf4j.LoggerFactory.getLogger(getClass()).error(exception.getMessage(), exception);
        }

        /**
         * Creates an {@link ErrorDetails} object containing the exception details and
         * request information.
         *
         * @param exception The exception to include in the error details.
         * @param request   The web request during which the exception occurred.
         * @return An {@link ErrorDetails} object with the exception details.
         */
        private ErrorDetails getErrorDetails(Exception exception, WebRequest request) {
                return new ErrorDetails(
                                LocalDateTime.now(),
                                exception.getMessage(),
                                request.getDescription(false));
        }
}
