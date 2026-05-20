package com.openclassrooms.datashare.service;

import com.openclassrooms.datashare.configuration.BackblazeB2Properties;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URL;
import java.time.Duration;
import java.util.function.Consumer;

import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.CompleteMultipartUploadRequest;
import software.amazon.awssdk.services.s3.model.CreateMultipartUploadRequest;
import software.amazon.awssdk.services.s3.model.CreateMultipartUploadResponse;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.UploadPartRequest;
import software.amazon.awssdk.services.s3.model.UploadPartResponse;
import software.amazon.awssdk.core.sync.RequestBody;

import org.mockito.Mock;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;

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

    private static final String EMAIL = "john.doe@example.com";
    private static final String FILENAME = "test-file.txt";
    private static final String FILE_CONTENT = "This is a test file content.";
    private static final String FILE_KEY = "uploads/" + EMAIL + "/" + FILENAME;
    private static final String ANONYMOUS_FILE_KEY = "uploads/anonymous/" + FILENAME;
    private static final String BUCKET_NAME = "test-bucket";
    private static final String ENDPOINT = "https://s3.test.com";
    private static final String REGION = "us-west-001";
    private static final String KEY_ID = "test-key-id";
    private static final String APPLICATION_KEY = "test-application-key";

    @Mock
    private BackblazeB2Properties properties;

    @Mock
    private S3Client s3Client;

    @Mock
    private MultipartFile multipartFile;

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

        @Nested
        @Tag("uploadFile-simpleUpload")
        @DisplayName("Tests for uploadFile on simple file upload")
        class UploadFileSimpleTests {
            @Test
            @DisplayName("given a multipart file smaller than the multipart threshold and a valid email, when uploadFile is called, then the file should be uploaded using a simple upload")
            public void test_uploadFile_small_file_and_valid_email_should_use_simple_upload() throws IOException {
                // Given
                when(multipartFile.getSize()).thenReturn(5_000_000L); // 5 Mo
                when(multipartFile.getOriginalFilename()).thenReturn(FILENAME);
                when(multipartFile.getContentType()).thenReturn("text/plain");
                when(multipartFile.getInputStream()).thenReturn(new ByteArrayInputStream(FILE_CONTENT.getBytes()));

                // When
                String uploadedKey = backblazeB2Service.uploadFile(multipartFile, EMAIL);

                // Then
                assertThat(uploadedKey).startsWith(FILE_KEY);
                verify(s3Client, times(1)).putObject(any(PutObjectRequest.class), any(RequestBody.class));
            }

            @Test
            @DisplayName("given a multipart file smaller than the multipart threshold and empty email, when uploadFile is called, then the file should be uploaded using a simple upload")
            public void test_uploadFile_small_file_and_empty_email_should_use_simple_upload() throws IOException {
                // Given
                when(multipartFile.getSize()).thenReturn(5_000_000L); // 5 Mo
                when(multipartFile.getOriginalFilename()).thenReturn(FILENAME);
                when(multipartFile.getContentType()).thenReturn("text/plain");
                when(multipartFile.getInputStream()).thenReturn(new ByteArrayInputStream(FILE_CONTENT.getBytes()));

                // When
                String uploadedKey = backblazeB2Service.uploadFile(multipartFile, null);

                // Then
                assertThat(uploadedKey).startsWith(ANONYMOUS_FILE_KEY);
                verify(s3Client, times(1)).putObject(any(PutObjectRequest.class), any(RequestBody.class));
            }
        }

        @Nested
        @Tag("uploadFile-Multipart")
        @DisplayName("Tests for uploadFile with multipart files")
        class UploadFileMultipartTests {

            @Test
            @DisplayName("Given a multipart file and email, when uploadFile is called, then the file should be uploaded in multipart")
            public void test_uploadFile_multipart_and_valid_email_shouldUploadFile() throws IOException {
                // Given
                when(multipartFile.getSize()).thenReturn(100_000_000L); // 100 Mo
                when(multipartFile.getOriginalFilename()).thenReturn(FILENAME);
                when(multipartFile.getContentType()).thenReturn("text/plain");
                when(multipartFile.getInputStream()).thenReturn(new ByteArrayInputStream(FILE_CONTENT.getBytes()));

                // Mock multipart upload
                when(s3Client.createMultipartUpload(any(CreateMultipartUploadRequest.class)))
                        .thenReturn(CreateMultipartUploadResponse.builder().uploadId("upload-id").build());
                when(s3Client.uploadPart(any(UploadPartRequest.class), any(RequestBody.class)))
                        .thenReturn(UploadPartResponse.builder().eTag("etag-1").build());
                when(s3Client.completeMultipartUpload(any(CompleteMultipartUploadRequest.class)))
                        .thenReturn(null);

                // When
                String uploadedKey = backblazeB2Service.uploadFile(multipartFile, EMAIL);

                // Then
                assertThat(uploadedKey).startsWith(FILE_KEY);
                verify(s3Client, times(1)).createMultipartUpload(any(CreateMultipartUploadRequest.class));
                verify(s3Client, atLeastOnce()).uploadPart(any(UploadPartRequest.class), any(RequestBody.class));
                verify(s3Client, times(1)).completeMultipartUpload(any(CompleteMultipartUploadRequest.class));
            }

            @Test
            @DisplayName("Given a multipart file and no email, when uploadFile is called, then the file should be uploaded in multipart")
            public void test_uploadFile_multipart_and_empty_email_shouldUploadFile() throws IOException {
                // Given
                when(multipartFile.getSize()).thenReturn(100_000_000L); // 100 Mo
                when(multipartFile.getOriginalFilename()).thenReturn(FILENAME);
                when(multipartFile.getContentType()).thenReturn("text/plain");
                when(multipartFile.getInputStream()).thenReturn(new ByteArrayInputStream(FILE_CONTENT.getBytes()));

                // Mock multipart upload
                when(s3Client.createMultipartUpload(any(CreateMultipartUploadRequest.class)))
                        .thenReturn(CreateMultipartUploadResponse.builder().uploadId("upload-id").build());
                when(s3Client.uploadPart(any(UploadPartRequest.class), any(RequestBody.class)))
                        .thenReturn(UploadPartResponse.builder().eTag("etag-1").build());
                when(s3Client.completeMultipartUpload(any(CompleteMultipartUploadRequest.class)))
                        .thenReturn(null);

                // When
                String uploadedKey = backblazeB2Service.uploadFile(multipartFile, null);

                // Then
                assertThat(uploadedKey).startsWith(ANONYMOUS_FILE_KEY);
                verify(s3Client, times(1)).createMultipartUpload(any(CreateMultipartUploadRequest.class));
                verify(s3Client, atLeastOnce()).uploadPart(any(UploadPartRequest.class), any(RequestBody.class));
                verify(s3Client, times(1)).completeMultipartUpload(any(CompleteMultipartUploadRequest.class));
            }
        }

        @Nested
        @Tag("uploadFile-errors")
        @DisplayName("Tests for uploadFile with error cases")
        class UploadFileErrorTests {
            @Test
            @DisplayName("Given an empty file,when uploadFile is called, then an exception should be thrown")
            public void test_uploadFile_empty_file_should_throw_Exception() {
                // GIVEN
                when(multipartFile.getSize()).thenReturn(0L);

                // WHEN & THEN
                assertThrows(IllegalArgumentException.class, () -> {
                    backblazeB2Service.uploadFile(multipartFile, EMAIL);
                });
            }

            @Test
            @DisplayName("when uploadFile throws an IOException, then a RuntimeException should be thrown")
            public void test_uploadFile_ioException_should_throw_RuntimeException() throws IOException {
                // GIVEN
                when(multipartFile.getSize()).thenReturn(5_000_000L);
                when(multipartFile.getOriginalFilename()).thenReturn(FILENAME);
                when(multipartFile.getInputStream()).thenThrow(new IOException());

                // WHEN & THEN
                assertThrows(RuntimeException.class, () -> {
                    backblazeB2Service.uploadFile(multipartFile, EMAIL);
                });
            }

        }

    }

    @Nested
    @Tag("generatePresignedUrl")
    @DisplayName("Tests for generatePresignedUrl method")
    class GeneratePresignedUrlTests {

        @Test
        @DisplayName("Given a valid file key and duration,when generatePresignedUrl is called, then a presigned URL should be returned")
        public void test_generatePresignedUrl_valid_file_key_and_duration_should_return_presigned_url() {
            // WHEN
            URL presignedUrl = backblazeB2Service.generatePresignedUrl(ANONYMOUS_FILE_KEY, Duration.ofDays(7));

            // THEN
            Assertions.assertNotNull(presignedUrl);
            Assertions
                    .assertTrue(presignedUrl.toString().contains(ANONYMOUS_FILE_KEY));
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
            assertThrows(
                    RuntimeException.class,
                    () -> {
                        backblazeB2Service.deleteFile(FILE_KEY);
                    });
        }
    }
}