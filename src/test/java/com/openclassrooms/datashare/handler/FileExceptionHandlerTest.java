package com.openclassrooms.datashare.handler;

import com.openclassrooms.datashare.handler.exceptions.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import org.springframework.http.HttpStatus;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;

@Tag("FileExceptionHandlerTest")
@DisplayName("Tests for FileExceptionHandler")
public class FileExceptionHandlerTest {

    private final FileExceptionHandler fileExceptionHandler = new FileExceptionHandler();
    private final String testUri = "/api/files/info/123";

    private WebRequest buildRequest(String uri) {
        return new ServletWebRequest(new org.springframework.mock.web.MockHttpServletRequest("GET", uri));
    }

    @Test
    @DisplayName("Given FileNotFoundException, when handleFileNotFoundException is called, then 404 with ErrorDetails is returned.")
    public void test_handleFileNotFoundException_should_return_404_with_ErrorDetails() {
        // GIVEN
        String exceptionMsg = "File not found";
        FileNotFoundException exception = new FileNotFoundException(exceptionMsg);

        // WHEN
        var response = fileExceptionHandler.handleFileNotFoundException(exception, buildRequest(testUri));

        // THEN
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        ErrorDetails body = assertInstanceOf(ErrorDetails.class, response.getBody());
        assertEquals(exceptionMsg, body.getMessage());
        assertEquals("uri=" + testUri, body.getDetails());
        assertNotNull(body.getTimestamp());
    }

    @Test
    @DisplayName("Given FileExpiredException, when handleFileExpiredException is called, then 410 with ErrorDetails is returned.")
    public void test_handleFileExpiredException_should_return_410_with_ErrorDetails() {
        // GIVEN
        String exceptionMsg = "File expired";
        FileExpiredException exception = new FileExpiredException(exceptionMsg);

        // WHEN
        var response = fileExceptionHandler.handleFileExpiredException(exception, buildRequest(testUri));

        // THEN
        assertEquals(HttpStatus.GONE, response.getStatusCode());
        ErrorDetails body = assertInstanceOf(ErrorDetails.class, response.getBody());
        assertEquals(exceptionMsg, body.getMessage());
        assertEquals("uri=" + testUri, body.getDetails());
        assertNotNull(body.getTimestamp());
    }

    @Test
    @DisplayName("Given InvalidPasswordException, when handleInvalidPasswordException is called, then 401 with ErrorDetails is returned.")
    public void test_handleInvalidPasswordException_should_return_401_with_ErrorDetails() {
        // GIVEN
        String exceptionMsg = "Invalid password";
        InvalidPasswordException exception = new InvalidPasswordException(exceptionMsg);

        // WHEN
        var response = fileExceptionHandler.handleInvalidPasswordException(exception, buildRequest(testUri));

        // THEN
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        ErrorDetails body = assertInstanceOf(ErrorDetails.class, response.getBody());
        assertEquals(exceptionMsg, body.getMessage());
        assertEquals("uri=" + testUri, body.getDetails());
        assertNotNull(body.getTimestamp());
    }

    @Test
    @DisplayName("Given FileLinkNullException, when handleFileLinkNullException is called, then 500 with ErrorDetails is returned.")
    public void test_handleFileLinkNullException_should_return_500_with_ErrorDetails() {
        // GIVEN
        String exceptionMsg = "File link is null";
        FileLinkNullException exception = new FileLinkNullException(exceptionMsg);

        // WHEN
        var response = fileExceptionHandler.handleFileLinkNullException(exception, buildRequest(testUri));

        // THEN
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        ErrorDetails body = assertInstanceOf(ErrorDetails.class, response.getBody());
        assertEquals(exceptionMsg, body.getMessage());
        assertEquals("uri=" + testUri, body.getDetails());
        assertNotNull(body.getTimestamp());
    }

    @Test
    @DisplayName("Given FileHashComputationException, when handleFileHashComputationException is called, then 400 with ErrorDetails is returned.")
    public void test_handleFileHashComputationException_should_return_400_with_ErrorDetails() {
        // GIVEN
        String exceptionMsg = "File hash computation failed";
        FileHashComputationException exception = new FileHashComputationException(exceptionMsg);

        // WHEN
        var response = fileExceptionHandler.handleFileHashComputationException(exception, buildRequest(testUri));

        // THEN
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        ErrorDetails body = assertInstanceOf(ErrorDetails.class, response.getBody());
        assertEquals(exceptionMsg, body.getMessage());
        assertEquals("uri=" + testUri, body.getDetails());
        assertNotNull(body.getTimestamp());
    }

    @Test
    @DisplayName("Given FileHashMismatchException, when handleFileHashMismatchException is called, then 400 with ErrorDetails is returned.")
    public void test_handleFileHashMismatchException_should_return_400_with_ErrorDetails() {
        // GIVEN
        String exceptionMsg = "File hash mismatch";
        FileHashMismatchException exception = new FileHashMismatchException(exceptionMsg);

        // WHEN
        var response = fileExceptionHandler.handleFileHashMismatchException(exception, buildRequest(testUri));

        // THEN
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        ErrorDetails body = assertInstanceOf(ErrorDetails.class, response.getBody());
        assertEquals(exceptionMsg, body.getMessage());
        assertEquals("uri=" + testUri, body.getDetails());
        assertNotNull(body.getTimestamp());
    }

    @Test
    @DisplayName("Given FileLinkGenerationException, when handleFileLinkGenerationException is called, then 500 with ErrorDetails is returned.")
    public void test_handleFileLinkGenerationException_should_return_500_with_ErrorDetails() {
        // GIVEN
        String exceptionMsg = "File link generation failed";
        FileLinkGenerationException exception = new FileLinkGenerationException(exceptionMsg);

        // WHEN
        var response = fileExceptionHandler.handleFileLinkGenerationException(exception, buildRequest(testUri));

        // THEN
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        ErrorDetails body = assertInstanceOf(ErrorDetails.class, response.getBody());
        assertEquals(exceptionMsg, body.getMessage());
        assertEquals("uri=" + testUri, body.getDetails());
        assertNotNull(body.getTimestamp());
    }

    @Test
    @DisplayName("Given FileDeletionException, when handleFileDeletionException is called, then 500 with ErrorDetails is returned.")
    public void test_handleFileDeletionException_should_return_500_with_ErrorDetails() {
        // GIVEN
        String exceptionMsg = "File deletion failed";
        FileDeletionException exception = new FileDeletionException(exceptionMsg);

        // WHEN
        var response = fileExceptionHandler.handleFileDeletionException(exception, buildRequest(testUri));

        // THEN
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        ErrorDetails body = assertInstanceOf(ErrorDetails.class, response.getBody());
        assertEquals(exceptionMsg, body.getMessage());
        assertEquals("uri=" + testUri, body.getDetails());
        assertNotNull(body.getTimestamp());
    }

}
