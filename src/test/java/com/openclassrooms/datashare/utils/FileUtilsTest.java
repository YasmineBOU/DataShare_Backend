package com.openclassrooms.datashare.utils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.springframework.mock.web.MockMultipartFile;
import java.io.IOException;
import java.util.Base64;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.MockedStatic;

import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@Tag("FileServiceTest")
@DisplayName("Tests for FileService")
public class FileUtilsTest {

    @Nested
    @Tag("calculateFileHash")
    @DisplayName("Tests for calculateFileHash method")
    class CalculateFileHashTest {

        @Test
        @DisplayName("Given null file, when calculateFileHash is called, then IllegalArgumentException is thrown.")
        public void test_calculateFileHash_with_null_file_should_throw_IllegalArgumentException() {
            // WHEN
            Assertions.assertThrows(
                    IllegalArgumentException.class,
                    () -> FileUtils.calculateFileHash(null));
        }

        @Test
        @DisplayName("Given a valid file, when calculateFileHash is called, then a hash string is returned.")
        public void test_calculateFileHash_with_valid_file_should_return_hash() throws IOException {
            // GIVEN
            MockMultipartFile file = new MockMultipartFile(
                    "file", "test.txt", "text/plain", "Hello world!".getBytes());

            // WHEN
            String hash = FileUtils.calculateFileHash(file);

            // THEN
            assertThat(hash).isNotNull()
                    .hasSize(64)
                    .matches("^[a-f0-9]{64}$");

        }

        @Test
        @DisplayName("Given an empty file, when calculateFileHash is called, then a hash string is returned.")
        public void test_calculateFileHash_with_empty_file_should_return_hash() throws IOException {
            // GIVEN
            MockMultipartFile file = new MockMultipartFile(
                    "file", "test.txt", "text/plain", new byte[0]);

            // WHEN
            String hash = FileUtils.calculateFileHash(file);

            // THEN
            assertThat(hash).isNotNull()
                    .hasSize(64)
                    .matches("^[a-f0-9]{64}$");

        }

    }

    @Nested
    @Tag("generateUniqueFileToken")
    @DisplayName("Tests for generateUniqueFileToken method")
    class GenerateUniqueFileTokenTest {

        String HASH = "abcdef1234567890";
        String FILE_KEY = "filekey12345";
        int TOKEN_LENGTH = 20;

        @Test
        @DisplayName("Given valid input, when generateUniqueFileToken is called, then a token is returned.")
        public void test_generateUniqueFileToken_should_return_token() {
            // GIVEN
            String expectedToken = "mockedBase64EncodedString";

            try (MockedStatic<Base64> mockedBase64 = mockStatic(Base64.class)) {
                Base64.Encoder encoderMock = mock(Base64.Encoder.class);
                mockedBase64.when(Base64::getUrlEncoder).thenReturn(encoderMock);
                when(encoderMock.withoutPadding()).thenReturn(encoderMock);
                when(encoderMock.encodeToString(any(byte[].class))).thenReturn(expectedToken);

                // WHEN
                String token = FileUtils.generateUniqueFileToken(HASH, FILE_KEY, TOKEN_LENGTH);

                // THEN
                assertThat(token.length()).isLessThanOrEqualTo(TOKEN_LENGTH);
                assertThat(token).isEqualTo(expectedToken.substring(0, TOKEN_LENGTH));
            }
        }

        @Test
        @DisplayName("Given NoSuchAlgorithmException, when generateUniqueFileToken is called, then IllegalStateException is thrown.")
        public void test_generateUniqueFileToken_with_noSuchAlgorithmException_should_throw_IllegalStateException()
                throws NoSuchAlgorithmException {
            // GIVEN
            try (MockedStatic<MessageDigest> mockedMessageDigest = mockStatic(MessageDigest.class)) {
                mockedMessageDigest.when(() -> MessageDigest.getInstance("SHA-256"))
                        .thenThrow(new NoSuchAlgorithmException("SHA-256 not available"));

                // THEN
                Assertions.assertThrows(
                        IllegalStateException.class,
                        () -> FileUtils.generateUniqueFileToken(HASH, FILE_KEY, TOKEN_LENGTH));
            }
        }

    }

    @Nested
    @Tag("getFileExtension")
    @DisplayName("Tests for getFileExtension method")
    class GetFileExtensionTest {

        @Test
        @DisplayName("Given null filename, when getFileExtension is called, then an empty string is returned.")
        public void test_getFileExtension_with_null_filename_should_return_empty_string() {
            // WHEN
            String extension = FileUtils.getFileExtension(null);

            // THEN
            assertThat(extension).isEmpty();
        }

        @Test
        @DisplayName("Given filename without extension, when getFileExtension is called, then an empty string is returned.")
        public void test_getFileExtension_with_filename_without_extension_should_return_empty_string() {
            // WHEN
            String extension = FileUtils.getFileExtension("test");

            // THEN
            assertThat(extension).isEmpty();
        }

        @Test
        @DisplayName("Given filename with extension, when getFileExtension is called, then the extension is returned.")
        public void test_getFileExtension_with_filename_with_extension_should_return_extension() {
            // WHEN
            String extension = FileUtils.getFileExtension("test.txt");

            // THEN
            assertThat(extension).isEqualTo(".txt");
        }
    }
}
