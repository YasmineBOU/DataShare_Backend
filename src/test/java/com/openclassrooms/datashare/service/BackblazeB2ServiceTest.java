package com.openclassrooms.datashare.service;

import com.openclassrooms.datashare.configuration.BackblazeB2Properties;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.time.Duration;
import java.util.function.Consumer;

import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;

import org.mockito.Mock;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import org.mockito.junit.jupiter.MockitoExtension;

import com.openclassrooms.datashare.configuration.BackblazeB2Properties;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@ExtendWith(MockitoExtension.class)
@Tag("BackblazeB2ServiceTest")
@DisplayName("Tests for BackblazeB2Service")
public class BackblazeB2ServiceTest {

    private static final long MULTIPART_THRESHOLD_BYTES = 8L * 1024 * 1024;
    private static final int MULTIPART_CHUNK_SIZE = 8 * 1024 * 1024;
    private static final String FILE_KEY = "test-file-key";
    private static final URL PRESIGNED_URL;
    // private static final URL PRESIGNED_URL = new
    // URLConnection("http://example.com/test-file-key").getURL();
    // private static final URL PRESIGNED_URL = new
    // URI("http://example.com/test-file-key").toURL();
    // private static final URL PRESIGNED_URL = new
    // URL("http://example.com/test-file-key");
    static {
        try {
            PRESIGNED_URL = new URI("http://example.com/test-file-key").toURL();
        } catch (URISyntaxException | MalformedURLException e) {
            throw new ExceptionInInitializerError("Failed to initialize PRESIGNED_URL : " + e.getMessage());
        }
    }

    private static final String BUCKET_NAME = "test-bucket";
    private static final String ENDPOINT = "https://s3.test.com";
    private static final String REGION = "us-west-001";
    private static final String KEY_ID = "test-key-id";
    private static final String APPLICATION_KEY = "test-application-key";

    @Mock
    private BackblazeB2Properties properties;

    @Mock
    private S3Client s3Client;

    private BackblazeB2Service backblazeB2Service;

    @BeforeEach
    public void setUp() {
        lenient().when(properties.getBucketName()).thenReturn(BUCKET_NAME);
        lenient().when(properties.getEndpoint()).thenReturn(ENDPOINT);
        lenient().when(properties.getRegion()).thenReturn(REGION);
        lenient().when(properties.getKeyId()).thenReturn(KEY_ID);
        lenient().when(properties.getApplicationKey()).thenReturn(APPLICATION_KEY);
        backblazeB2Service = new BackblazeB2Service(s3Client, properties);
    }

    @Nested
    @Tag("uploadFile")
    @DisplayName("Tests for uploadFile method")
    class UploadFileTests {

    }

    @Nested
    @Tag("generatePresignedUrl")
    @DisplayName("Tests for generatePresignedUrl method")
    class GeneratePresignedUrlTests {

        @Test
        @DisplayName("when generatePresignedUrl is called with a valid file key and duration, then a presigned URL should be returned")
        public void test_generatePresignedUrl_valid_file_key_and_duration_should_return_presigned_url() {
            // WHEN
            URL presignedUrl = backblazeB2Service.generatePresignedUrl(FILE_KEY, Duration.ofDays(7));

            // THEN
            Assertions.assertNotNull(presignedUrl);
            Assertions.assertTrue(presignedUrl.toString().contains(FILE_KEY));
            Assertions.assertTrue(presignedUrl.toString().contains("X-Amz-Algorithm"));
        }
    }

    @Nested
    @Tag("deleteFile")
    @DisplayName("Tests for deleteFile method")
    class DeleteFileTests {
        @Test
        @DisplayName("when deleteFile is called with a valid file key, then the file should be deleted successfully")
        public void test_deleteFile_valid_file_key_should_delete_file() {
            // When
            backblazeB2Service.deleteFile(FILE_KEY);

            // Then
            verify(s3Client, times(1))
                    .deleteObject(org.mockito.ArgumentMatchers.<Consumer<DeleteObjectRequest.Builder>>any());
        }

        @Test
        @DisplayName("when deleteFile is called with a null file key, then no deletion should occur")
        public void test_deleteFile_null_file_key_should_not_delete_file() {
            // When
            backblazeB2Service.deleteFile(null);

            // Then
            verify(s3Client, never())
                    .deleteObject(any(DeleteObjectRequest.class));
        }

        @Test
        @DisplayName("Given an unexpected exception, when deletedFile is called, then the exception is thrown")
        public void test_deleteFile_unexpected_exception_should_throw_exception() {
            // Given
            when(s3Client.deleteObject(any(DeleteObjectRequest.class))).thenThrow(new RuntimeException());

            // THEN
            Assertions.assertThrows(
                    RuntimeException.class,
                    () -> {
                        backblazeB2Service.deleteFile(FILE_KEY);
                    });
        }
    }
}