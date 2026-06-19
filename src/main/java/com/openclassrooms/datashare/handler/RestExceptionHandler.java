package com.openclassrooms.datashare.handler;

import java.nio.file.AccessDeniedException;
import java.time.LocalDateTime;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import com.openclassrooms.datashare.handler.exceptions.UserAlreadyExistsException;

/**
 * Global exception handler for REST-related exceptions.
 * This class extends {@link ResponseEntityExceptionHandler} and uses Spring's
 * {@link RestControllerAdvice}
 * to centralize the handling of exceptions thrown by controllers or services in
 * the application.
 * It converts specific exceptions into standardized {@link ResponseEntity}
 * objects with appropriate
 * HTTP status codes and error details.
 *
 * <p>
 * Key functionalities include:
 * <ul>
 * <li>Handling exceptions such as {@link IllegalArgumentException},
 * {@link BadCredentialsException}, etc.</li>
 * <li>Returning appropriate HTTP status codes for each type of exception.</li>
 * <li>Generating structured error details using {@link ErrorDetails}.</li>
 * <li>Logging exceptions for debugging and monitoring purposes.</li>
 * </ul>
 *
 * @see RestControllerAdvice
 * @see ResponseEntityExceptionHandler
 * @see ErrorDetails
 */
@RestControllerAdvice
public class RestExceptionHandler extends ResponseEntityExceptionHandler {

    /**
     * Handles {@link IllegalArgumentException} and {@link IllegalStateException} by
     * returning a response
     * with HTTP status 400 (Bad Request).
     *
     * @param runtimeException The exception to handle.
     * @param request          The web request during which the exception occurred.
     * @return A {@link ResponseEntity} with HTTP status 400 and error details.
     */
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(value = { IllegalArgumentException.class, IllegalStateException.class })
    protected ResponseEntity<Object> handleConflict(RuntimeException runtimeException, WebRequest request) {
        logError(runtimeException);
        return handleExceptionInternal(runtimeException, getErrorDetails(runtimeException, request), new HttpHeaders(),
                HttpStatus.BAD_REQUEST, request);
    }

    /**
     * Handles {@link UserAlreadyExistsException} by returning a response with HTTP
     * status 409 (Conflict).
     *
     * @param userAlreadyExistsException The exception to handle.
     * @param request                    The web request during which the exception
     *                                   occurred.
     * @return A {@link ResponseEntity} with HTTP status 409 and error details.
     */
    @ResponseStatus(HttpStatus.CONFLICT)
    @ExceptionHandler(value = { UserAlreadyExistsException.class })
    protected ResponseEntity<Object> handleUserAlreadyExistsException(
            UserAlreadyExistsException userAlreadyExistsException,
            WebRequest request) {
        logError(userAlreadyExistsException);
        return handleExceptionInternal(userAlreadyExistsException, getErrorDetails(userAlreadyExistsException, request),
                new HttpHeaders(), HttpStatus.CONFLICT, request);
    }

    /**
     * Handles {@link BadCredentialsException} by returning a response with HTTP
     * status 401 (Unauthorized).
     *
     * @param badCredentialsException The exception to handle.
     * @param request                 The web request during which the exception
     *                                occurred.
     * @return A {@link ResponseEntity} with HTTP status 401 and error details.
     */
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    @ExceptionHandler(value = { BadCredentialsException.class })
    protected ResponseEntity<Object> handleBadCredentialsException(BadCredentialsException badCredentialsException,
            WebRequest request) {
        logError(badCredentialsException);
        return handleExceptionInternal(badCredentialsException, getErrorDetails(badCredentialsException, request),
                new HttpHeaders(), HttpStatus.UNAUTHORIZED, request);
    }

    /**
     * Handles {@link AccessDeniedException} by returning a response with HTTP
     * status 403 (Forbidden).
     *
     * @param accessDeniedException The exception to handle.
     * @param request               The web request during which the exception
     *                              occurred.
     * @return A {@link ResponseEntity} with HTTP status 403 and error details.
     */
    @ResponseStatus(HttpStatus.FORBIDDEN)
    @ExceptionHandler(value = { AccessDeniedException.class })
    protected ResponseEntity<Object> handleForbiddenException(AccessDeniedException accessDeniedException,
            WebRequest request) {
        logError(accessDeniedException);
        return handleExceptionInternal(accessDeniedException, getErrorDetails(accessDeniedException, request),
                new HttpHeaders(), HttpStatus.FORBIDDEN, request);
    }

    /**
     * Handles generic {@link Exception} by returning a response with HTTP status
     * 500 (Internal Server Error).
     * This method acts as a fallback to ensure no unhandled exceptions propagate to
     * the client.
     *
     * @param runtimeException The exception to handle.
     * @param request          The web request during which the exception occurred.
     * @return A {@link ResponseEntity} with HTTP status 500 and a generic error
     *         message.
     */
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(value = { Exception.class })
    protected ResponseEntity<Object> handleException(RuntimeException runtimeException, WebRequest request) {
        logError(runtimeException);
        return handleExceptionInternal(runtimeException, "Internal Server error", new HttpHeaders(),
                HttpStatus.INTERNAL_SERVER_ERROR, request);
    }

    /**
     * Logs the exception for debugging and monitoring purposes.
     *
     * @param exception The exception to log.
     */
    private void logError(Exception exception) {
        logger.error(exception.getMessage(), exception);
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
        return new ErrorDetails(LocalDateTime.now(), exception.getMessage(), request.getDescription(false));
    }
}
