package com.openclassrooms.datashare.service;

import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.Mock;

import java.io.IOException;
import java.net.URL;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.stream.Stream;
import java.util.ArrayList;
import java.util.List;

import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.multipart.MultipartFile;

import com.openclassrooms.datashare.configuration.FileProperties;
import com.openclassrooms.datashare.dto.FileInfoDTO;
import com.openclassrooms.datashare.entities.FileData;
import com.openclassrooms.datashare.entities.User;
import com.openclassrooms.datashare.handler.exceptions.*;
import com.openclassrooms.datashare.repository.FileRepository;
import com.openclassrooms.datashare.repository.UserRepository;
import com.openclassrooms.datashare.utils.FileUtils;

@ExtendWith(MockitoExtension.class)
@Tag("FileServiceTest")
@DisplayName("Tests for FileService")
public class FileServiceTest {

        private static final Long VALID_FILE_ID = 1L;
        private static final String VALID_FILE_TOKEN = "existing-token";
        private static final String EMAIL = "john.doe@example.com";
        private static final String PASSWORD = "password";
        private static final String FILE_TOKEN = "KcAolhUCkkkalqFcQRmx";

        @Mock
        private BackblazeB2Service backblazeB2Service;
        @Mock
        private FileRepository fileRepository;
        @Mock
        private UserRepository userRepository;
        @Mock
        private PasswordEncoder passwordEncoder;
        @Mock
        private FileProperties fileProperties;

        @InjectMocks
        private FileService fileService;

        @Nested
        @Tag("uploadFile")
        @DisplayName("Tests for uploadFile method")
        class UploadFileTests {

                private static MultipartFile UPLOADED_FILE = new MockMultipartFile("file", "file.txt", "text/plain",
                                "Hello, World!".getBytes());

                private static FileData FILE_DATA = new FileData();
                private static int EXPIRATION_DAYS = 1;
                private static String FILE_HASH = "calculatedHash";
                private static String FILE_KEY = "fileKey";
                private static String FILE_EXT = "txt";

                private MockedStatic<FileUtils> mockedFileUtils;

                private static Stream<Arguments> provideInvalidInput() {
                        return Stream.of(
                                        // uploadFile = null, fileData = any, expirationDays = any
                                        Arguments.of(null, new FileData(), 1),
                                        // uploadFile = any, fileData = null, expirationDays = any
                                        Arguments.of(new MockMultipartFile("file", new byte[0]), null, 1),
                                        // uploadFile = any, fileData = any, expirationDays =< 0
                                        Arguments.of(new MockMultipartFile("file", new byte[0]), new FileData(), -1),
                                        Arguments.of(new MockMultipartFile("file", new byte[0]), new FileData(), 0));
                }

                @BeforeEach
                void setUp() {
                        mockedFileUtils = Mockito.mockStatic(FileUtils.class);
                }

                @AfterEach
                public void tearDown() {
                        mockedFileUtils.close();
                }

                @ParameterizedTest()
                @MethodSource("provideInvalidInput")
                @DisplayName("Given an invalid authenticated user and/or id, when deleteFile is called, then IllegalArgumentException is thrown.")
                public void test_deleteFile_invalid_input_throws_IllegalArgumentException(MultipartFile uploadedFile,
                                FileData fileData, int expirationDays) {
                        // THEN
                        Assertions.assertThrows(
                                        IllegalArgumentException.class,
                                        () -> fileService.uploadFile(uploadedFile, fileData, expirationDays));
                }

                @Test
                @DisplayName("Given valid input data and hash mismatch occur, when uploadFile is called, then FileHashMismatchException is thrown.")
                public void test_uploadFile_valid_input_data_with_mismatch_hash_throws_FileHashMismatchException() {
                        // GIVEN
                        FILE_DATA.setHash(FILE_HASH);

                        when(fileProperties.getForbiddenExtensions()).thenReturn(List.of("exe", "bat"));

                        mockedFileUtils.when(
                                        () -> FileUtils.getFileExtension(any()))
                                        .thenReturn(FILE_EXT);
                        mockedFileUtils.when(
                                        () -> FileUtils.calculateFileHash(UPLOADED_FILE))
                                        .thenReturn("differentHash");

                        // THEN
                        Assertions.assertThrows(
                                        FileHashMismatchException.class,
                                        () -> fileService.uploadFile(UPLOADED_FILE, FILE_DATA, EXPIRATION_DAYS));
                }

                @Test
                @DisplayName("Given valid input data and an IOException occurs, when uploadFile is called, then RuntimeException is thrown.")
                public void test_uploadFile_valid_input_data_with_IOException_occurs_throws_RuntimeException()
                                throws IOException {
                        // GIVEN
                        FILE_DATA.setHash(FILE_HASH);

                        when(fileProperties.getForbiddenExtensions()).thenReturn(List.of("exe", "bat"));

                        mockedFileUtils.when(
                                        () -> FileUtils.getFileExtension(any()))
                                        .thenReturn(FILE_EXT);

                        mockedFileUtils.when(
                                        () -> FileUtils.calculateFileHash(UPLOADED_FILE))
                                        .thenReturn(FILE_HASH);

                        when(backblazeB2Service.uploadFile(UPLOADED_FILE, FILE_DATA.getEmail()))
                                        .thenThrow(new IOException());

                        // THEN
                        Assertions.assertThrows(
                                        RuntimeException.class,
                                        () -> fileService.uploadFile(UPLOADED_FILE, FILE_DATA, EXPIRATION_DAYS));
                }

                @Test
                @DisplayName("Given valid input data and a null URL is returned, when uploadFile is called, then FileLinkGenerationException is thrown.")
                public void test_uploadFile_valid_input_data_with_null_url_throws_FileLinkGenerationException()
                                throws IOException {
                        // GIVEN
                        FILE_DATA.setHash(FILE_HASH);

                        mockedFileUtils.when(
                                        () -> FileUtils.calculateFileHash(UPLOADED_FILE))
                                        .thenReturn(FILE_HASH);

                        when(fileProperties.getForbiddenExtensions()).thenReturn(List.of("exe", "bat"));

                        mockedFileUtils.when(
                                        () -> FileUtils.getFileExtension(any()))
                                        .thenReturn(FILE_EXT);

                        when(backblazeB2Service.uploadFile(UPLOADED_FILE, FILE_DATA.getEmail()))
                                        .thenReturn(FILE_KEY);
                        when(backblazeB2Service.generatePresignedUrl(FILE_KEY, Duration.ofDays(EXPIRATION_DAYS)))
                                        .thenReturn(null);

                        // THEN
                        Assertions.assertThrows(
                                        FileLinkGenerationException.class,
                                        () -> fileService.uploadFile(UPLOADED_FILE, FILE_DATA, EXPIRATION_DAYS));
                }

                @Test
                @DisplayName("Given valid input data and a empty URL is returned, when uploadFile is called, then FileLinkGenerationException is thrown.")
                public void test_uploadFile_valid_input_data_with_empty_url_throws_FileLinkGenerationException()
                                throws IOException {
                        // GIVEN
                        FILE_DATA.setHash(FILE_HASH);
                        URL emptyUrl = Mockito.mock(URL.class);

                        mockedFileUtils.when(
                                        () -> FileUtils.calculateFileHash(UPLOADED_FILE))
                                        .thenReturn(FILE_HASH);

                        when(fileProperties.getForbiddenExtensions()).thenReturn(List.of("exe", "bat"));

                        mockedFileUtils.when(
                                        () -> FileUtils.getFileExtension(any()))
                                        .thenReturn(FILE_EXT);

                        when(backblazeB2Service.uploadFile(UPLOADED_FILE, FILE_DATA.getEmail()))
                                        .thenReturn(FILE_KEY);
                        when(emptyUrl.toString()).thenReturn("");
                        when(backblazeB2Service.generatePresignedUrl(FILE_KEY, Duration.ofDays(EXPIRATION_DAYS)))
                                        .thenReturn(emptyUrl);

                        // THEN
                        Assertions.assertThrows(
                                        FileLinkGenerationException.class,
                                        () -> fileService.uploadFile(UPLOADED_FILE, FILE_DATA, EXPIRATION_DAYS));
                }

                @Test
                @DisplayName("Given valid input data with password, when uploadFile is called, then link is returned.")
                public void test_uploadFile_valid_input_data_with_password_returns_link()
                                throws IOException {
                        // GIVEN
                        FILE_DATA.setHash(FILE_HASH);
                        FILE_DATA.setFilePassword(PASSWORD);

                        mockedFileUtils.when(
                                        () -> FileUtils.calculateFileHash(UPLOADED_FILE))
                                        .thenReturn(FILE_HASH);

                        when(fileProperties.getForbiddenExtensions()).thenReturn(List.of("exe", "bat"));

                        mockedFileUtils.when(
                                        () -> FileUtils.getFileExtension(any()))
                                        .thenReturn(FILE_EXT);

                        when(backblazeB2Service.uploadFile(UPLOADED_FILE, FILE_DATA.getEmail()))
                                        .thenReturn(FILE_KEY);
                        when(backblazeB2Service.generatePresignedUrl(FILE_KEY, Duration.ofDays(EXPIRATION_DAYS)))
                                        .thenReturn(new URL("http://example.com/"));

                        mockedFileUtils.when(
                                        () -> FileUtils.generateUniqueFileToken(FILE_HASH, FILE_KEY, 20))
                                        .thenReturn(FILE_TOKEN);

                        // WHEN
                        String fileToken = fileService.uploadFile(UPLOADED_FILE, FILE_DATA, EXPIRATION_DAYS);

                        // THEN
                        verify(fileRepository, times(1)).save(FILE_DATA);
                        verify(passwordEncoder, times(1)).encode(PASSWORD);
                        assertThat(fileToken).isEqualTo(FILE_TOKEN);
                }

                @Test
                @DisplayName("Given valid input data without provided password, when uploadFile is called, thenlink is returned.")
                public void test_uploadFile_valid_input_data_without_password_returns_link()
                                throws IOException {
                        // GIVEN
                        FILE_DATA.setHash(FILE_HASH);

                        mockedFileUtils.when(
                                        () -> FileUtils.calculateFileHash(UPLOADED_FILE))
                                        .thenReturn(FILE_HASH);

                        when(fileProperties.getForbiddenExtensions()).thenReturn(List.of("exe", "bat"));

                        mockedFileUtils.when(
                                        () -> FileUtils.getFileExtension(any()))
                                        .thenReturn(FILE_EXT);

                        when(backblazeB2Service.uploadFile(UPLOADED_FILE, FILE_DATA.getEmail()))
                                        .thenReturn(FILE_KEY);
                        when(backblazeB2Service.generatePresignedUrl(FILE_KEY, Duration.ofDays(EXPIRATION_DAYS)))
                                        .thenReturn(new URL("http://example.com/"));

                        mockedFileUtils.when(
                                        () -> FileUtils.generateUniqueFileToken(FILE_HASH, FILE_KEY, 20))
                                        .thenReturn(FILE_TOKEN);

                        // WHEN
                        String fileToken = fileService.uploadFile(UPLOADED_FILE, FILE_DATA, EXPIRATION_DAYS);

                        // THEN
                        verify(fileRepository, times(1)).save(FILE_DATA);
                        verify(passwordEncoder, never()).encode(any());
                        assertThat(fileToken).isEqualTo(FILE_TOKEN);
                }

                @Test
                @DisplayName("Given file with forbidden extension, when uploadFile is called, then ForbiddenFileExtensionException is thrown.")
                public void test_uploadFile_with_forbidden_extension_throws_ForbiddenFileExtensionException() {
                        // GIVEN
                        String filename = "malware.exe";
                        MultipartFile uploadedFile = new MockMultipartFile(
                                        "file",
                                        filename,
                                        "application/octet-stream",
                                        new byte[1024]);

                        when(fileProperties.getForbiddenExtensions()).thenReturn(List.of("exe", "bat"));

                        mockedFileUtils.when(
                                        () -> FileUtils.getFileExtension(filename))
                                        .thenReturn("exe");

                        // THEN
                        Assertions.assertThrows(
                                        FileExtensionException.class,
                                        () -> fileService.uploadFile(uploadedFile, FILE_DATA,
                                                        EXPIRATION_DAYS));
                }
        }

        @Nested
        @Tag("downloadFile")
        @DisplayName("Tests for downloadFile method")
        class DownloadFileTests {

                @Test
                @DisplayName("Given a null id, when downloadFile is called, then IllegalArgumentException is thrown")
                public void test_downloadFile_null_id_throws_IllegalArgumentException() {
                        // THEN
                        Assertions.assertThrows(
                                        IllegalArgumentException.class,
                                        () -> fileService.downloadFile(null, "password"));

                }

                @Test
                @DisplayName("Given an unexisting id, when downloadFile is called, then FileNotFoundException is thrown")
                public void test_downloadFile_unexesting_id_throws_FileNotFoundException() {
                        // WHEN
                        Long unexistingId = 999L;
                        when(fileRepository.findById(unexistingId)).thenReturn(Optional.empty());

                        // THEN
                        Assertions.assertThrows(
                                        FileNotFoundException.class,
                                        () -> fileService.downloadFile(unexistingId, "password"));

                }

                @Test
                @DisplayName("Given an expired file, when downloadFile is called, then FileExpiredException is thrown")
                public void test_downloadFile_expired_file_throws_FileExpiredException() {
                        // GIVEN
                        FileData existingFile = new FileData();
                        existingFile.setId(VALID_FILE_ID);
                        existingFile.setExpirationDate(LocalDateTime.now().minusDays(1)); // Set expiration date in the
                                                                                          // past
                        when(fileRepository.findById(VALID_FILE_ID)).thenReturn(Optional.of(existingFile));

                        // THEN
                        Assertions.assertThrows(
                                        FileExpiredException.class,
                                        () -> fileService.downloadFile(VALID_FILE_ID, PASSWORD));

                }

                @Nested
                @Tag("downloadFile_invalid_password_handling")
                @DisplayName("Tests for downloadFile method - invalid password handling")
                class DownloadFileInvalidPasswordTests {

                        @Test
                        @DisplayName("Given a file without password, when downloadFile is called with a provided password, then InvalidPasswordException is thrown")
                        public void test_downloadFile_with_file_without_password_and_unexpected_password_provided_throws_InvalidPasswordException() {
                                // GIVEN
                                FileData existingFile = new FileData();
                                existingFile.setId(VALID_FILE_ID);
                                // Set expiration date in the future
                                existingFile.setExpirationDate(LocalDateTime.now().plusDays(1));
                                existingFile.setFilePassword(null); // No password set

                                when(fileRepository.findById(VALID_FILE_ID)).thenReturn(Optional.of(existingFile));

                                // THEN
                                Assertions.assertThrows(
                                                InvalidPasswordException.class,
                                                () -> fileService.downloadFile(VALID_FILE_ID, "providedPassword"));

                        }

                        @Test
                        @DisplayName("Given a protected file and a null password is provided, when downloadFile is called, then InvalidPasswordException is thrown")
                        public void test_downloadFile_with_protected_file_and_null_password_provided_throws_InvalidPasswordException() {
                                // GIVEN
                                FileData existingFile = new FileData();
                                existingFile.setId(VALID_FILE_ID);
                                // Set expiration date in the future
                                existingFile.setExpirationDate(LocalDateTime.now().plusDays(1));
                                existingFile.setFilePassword(PASSWORD);

                                when(fileRepository.findById(VALID_FILE_ID)).thenReturn(Optional.of(existingFile));

                                // THEN
                                Assertions.assertThrows(
                                                InvalidPasswordException.class,
                                                () -> fileService.downloadFile(VALID_FILE_ID, null));

                        }

                        @Test
                        @DisplayName("Given a protected file and a wrong password is provided, when downloadFile is called, then InvalidPasswordException is thrown")
                        public void test_downloadFile_with_protected_file_and_wrong_password_provided_throws_InvalidPasswordException() {
                                // GIVEN
                                FileData existingFile = new FileData();
                                existingFile.setId(VALID_FILE_ID);
                                existingFile.setExpirationDate(LocalDateTime.now().plusDays(1)); // Set expiration date
                                                                                                 // in the future
                                existingFile.setFilePassword(PASSWORD);
                                when(fileRepository.findById(VALID_FILE_ID)).thenReturn(Optional.of(existingFile));
                                when(passwordEncoder.matches("wrongPassword", existingFile.getFilePassword()))
                                                .thenReturn(false);

                                // THEN
                                Assertions.assertThrows(
                                                InvalidPasswordException.class,
                                                () -> fileService.downloadFile(VALID_FILE_ID, "wrongPassword"));

                        }
                }

                @Nested
                @Tag("downloadFile_valid_input_file")
                @DisplayName("Tests for downloadFile method - valid input/file")
                class DownloadFileValidInputTests {

                        @Test
                        @DisplayName("Given a protected valid file and correct id and password provided, when downloadFile is called, then the file link is returned")
                        public void test_downloadFile_protected_valid_file_with_valid_input_and_data_return_file_link() {
                                // GIVEN
                                FileData existingFile = new FileData();
                                existingFile.setId(VALID_FILE_ID);
                                // Set expiration date in the future
                                existingFile.setExpirationDate(LocalDateTime.now().plusDays(1));
                                existingFile.setFilePassword(PASSWORD);
                                existingFile.setFileLink(FILE_TOKEN);

                                when(fileRepository.findById(VALID_FILE_ID)).thenReturn(Optional.of(existingFile));
                                when(passwordEncoder.matches(PASSWORD, existingFile.getFilePassword()))
                                                .thenReturn(true);

                                // WHEN
                                String fileToken = fileService.downloadFile(VALID_FILE_ID, PASSWORD);

                                // THEN
                                verify(fileRepository, times(1)).findById(VALID_FILE_ID);
                                verify(passwordEncoder, times(1)).matches(PASSWORD, existingFile.getFilePassword());
                                assertThat(fileToken).isEqualTo(FILE_TOKEN);

                        }

                        @Test
                        @DisplayName("Given a non protected valid file and correct id and no password provided, when downloadFile is called, then the file link is returned")
                        public void test_downloadFile_non_protected_valid_file_with_valid_input_and_data_return_file_link() {
                                // GIVEN
                                FileData existingFile = new FileData();
                                existingFile.setId(VALID_FILE_ID);
                                existingFile.setExpirationDate(LocalDateTime.now().plusDays(1));
                                existingFile.setFilePassword(null);
                                existingFile.setFileLink(FILE_TOKEN);

                                when(fileRepository.findById(VALID_FILE_ID)).thenReturn(Optional.of(existingFile));

                                // WHEN
                                String fileToken = fileService.downloadFile(VALID_FILE_ID, null);

                                // THEN
                                verify(fileRepository, times(1)).findById(VALID_FILE_ID);
                                verify(passwordEncoder, never()).matches(PASSWORD, existingFile.getFilePassword());
                                assertThat(fileToken).isEqualTo(FILE_TOKEN);

                        }

                        @Test
                        @DisplayName("Given a protected valid file and correct id and password provided with empty fileToken, when downloadFile is called, then FileLinkNullException is thrown")
                        public void test_downloadFile_protected_valid_file_with_valid_input_and_data_throws_FileLinkNullException() {
                                // GIVEN
                                FileData existingFile = new FileData();
                                existingFile.setId(VALID_FILE_ID);
                                existingFile.setExpirationDate(LocalDateTime.now().plusDays(1));
                                existingFile.setFilePassword(PASSWORD);
                                existingFile.setFileLink(""); // Set file link to empty string

                                when(fileRepository.findById(VALID_FILE_ID)).thenReturn(Optional.of(existingFile));
                                when(passwordEncoder.matches(PASSWORD, existingFile.getFilePassword()))
                                                .thenReturn(true);

                                // WHEN
                                Assertions.assertThrows(
                                                FileLinkNullException.class,
                                                () -> fileService.downloadFile(VALID_FILE_ID, PASSWORD));
                        }

                        @Test
                        @DisplayName("Given a non protected valid file and correct id and no password provided with empty fileToken, when downloadFile is called, then FileLinkNullException is thrown")
                        public void test_downloadFile_non_protected_valid_file_with_valid_input_and_data_throws_FileLinkNullException() {
                                // GIVEN
                                FileData existingFile = new FileData();
                                existingFile.setId(VALID_FILE_ID);
                                existingFile.setExpirationDate(LocalDateTime.now().plusDays(1));
                                existingFile.setFilePassword(null);
                                existingFile.setFileLink(""); // Set file link to empty string

                                when(fileRepository.findById(VALID_FILE_ID)).thenReturn(Optional.of(existingFile));

                                // WHEN
                                Assertions.assertThrows(
                                                FileLinkNullException.class,
                                                () -> fileService.downloadFile(VALID_FILE_ID, null));
                        }
                }
        }

        @Nested
        @Tag("listFiles")
        @DisplayName("Tests for listFiles method")
        class ListFilesTests {

                private static Stream<Arguments> provideInvalidUserOrEmail() {
                        return Stream.of(
                                        // authenticatedUser = null, email = any
                                        Arguments.of(null, EMAIL),

                                        // authenticatedUser = any, email = null
                                        Arguments.of(new User(), null));
                }

                @ParameterizedTest()
                @MethodSource("provideInvalidUserOrEmail")
                @DisplayName("Given a null authenticated user and/or email, when listFiles is called, then IllegalArgumentException is thrown.")
                public void test_listFiles_invalid_input_throws_IllegalArgumentException(User authenticatedUser,
                                String email) {
                        // THEN
                        Assertions.assertThrows(
                                        IllegalArgumentException.class,
                                        () -> fileService.listFiles(authenticatedUser, email));
                }

                @Test
                @DisplayName("Given a valid authenticated user and email, when listFiles is called, then the list of files is returned")
                public void test_listFiles_valid_input_return_list_of_files() {
                        // GIVEN
                        ArrayList<FileInfoDTO> expectedFiles = new ArrayList<FileInfoDTO>();
                        when(fileRepository.findFilesByEmail(EMAIL)).thenReturn(expectedFiles);

                        // WHEN
                        Iterable<FileInfoDTO> retrievedFiles = fileService.listFiles(new User(), EMAIL);

                        // THEN
                        verify(fileRepository, times(1)).findFilesByEmail(EMAIL);
                        assertThat(retrievedFiles).isEqualTo(expectedFiles);
                }

        }

        @Nested
        @Tag("getFileInfoByFileToken")
        @DisplayName("Tests for getFileInfoByFileToken method")
        class GetFileInfoTests {

                @Test
                @DisplayName("Given a null fileToken, when getFileInfoByFileToken is called, then IllegalArgumentException is thrown.")
                public void test_getFileInfoByFileToken_null_fileToken_throws_IllegalArgumentException() {
                        // THEN
                        Assertions.assertThrows(
                                        IllegalArgumentException.class,
                                        () -> fileService.getFileInfoByFileToken(null));
                }

                @Test
                @DisplayName("Given an unexisting fileToken, when getFileInfoByFileToken is called, then FileNotFoundException is thrown.")
                public void test_getFileInfoByFileToken_unexisting_fileToken_throws_FileNotFoundException() {
                        // THEN
                        Assertions.assertThrows(
                                        FileNotFoundException.class,
                                        () -> fileService.getFileInfoByFileToken("unexisting-token"));
                }

                @Test
                @DisplayName("Given an existing fileToken, when getFileInfoByFileToken is called, then FileInfoDTO is returned.")
                public void test_getFileInfoByFileToken_existing_fileToken_return_FileInfoDTO() {
                        // GIVEN
                        FileInfoDTO existingFile = new FileInfoDTO();
                        existingFile.setFileToken(VALID_FILE_TOKEN);
                        when(fileRepository.findFileInfoByFileToken(VALID_FILE_TOKEN))
                                        .thenReturn(Optional.of(existingFile));

                        // WHEN
                        FileInfoDTO fetchedFileInfo = fileService.getFileInfoByFileToken(VALID_FILE_TOKEN);

                        // THEN
                        verify(fileRepository, times(1)).findFileInfoByFileToken(VALID_FILE_TOKEN);
                        assertThat(fetchedFileInfo).isSameAs(existingFile);

                }
        }

        @Nested
        @Tag("deleteFile")
        @DisplayName("Tests for deleteFile method")
        class DeleteFileTests {

                private static Stream<Arguments> provideInvalidInput() {
                        return Stream.of(
                                        // authenticatedUser = null, id = any
                                        Arguments.of(null, 1L),

                                        // authenticatedUser = any, id = null or <= 0
                                        Arguments.of(new User(), null),
                                        Arguments.of(new User(), 0L),
                                        Arguments.of(new User(), -1L));
                }

                @ParameterizedTest()
                @MethodSource("provideInvalidInput")
                @DisplayName("Given an invalid authenticated user and/or id, when deleteFile is called, then IllegalArgumentException is thrown.")
                public void test_deleteFile_invalid_input_throws_IllegalArgumentException(User authenticatedUser,
                                Long id) {
                        // THEN
                        Assertions.assertThrows(
                                        IllegalArgumentException.class,
                                        () -> fileService.deleteFile(authenticatedUser, id));
                }

                @Test
                @DisplayName("Given an unexisting id, when deleteFile is called, then FileNotFoundException is thrown.")
                public void test_deleteFile_unexisting_id_throws_FileNotFoundException() {
                        // THEN
                        Assertions.assertThrows(
                                        FileNotFoundException.class,
                                        () -> fileService.deleteFile(new User(), 999L));
                }

                @Test
                @DisplayName("Given an valid id, when deleteFile is called, then the file is deleted.")
                public void test_deleteFile_existing_id_return_deletes_file() {
                        // GIVEN
                        FileData existingFile = new FileData();
                        existingFile.setId(VALID_FILE_ID);
                        when(fileRepository.findById(VALID_FILE_ID)).thenReturn(Optional.of(existingFile));

                        // WHEN
                        fileService.deleteFile(new User(), VALID_FILE_ID);

                        // THEN
                        verify(fileRepository, times(1)).findById(VALID_FILE_ID);
                        verify(backblazeB2Service, times(1)).deleteFile(existingFile.getFileKey());
                        verify(fileRepository, times(1)).delete(existingFile);

                }

                @Test
                @DisplayName("Given an existing id and an occured exception with deletion, when deleteFile is called, then FileDeletionException is thrown.")
                public void test_deleteFile_existing_id_with_occured_exception_return_FileInfoDTO() {
                        // GIVEN
                        FileData existingFile = new FileData();
                        existingFile.setId(VALID_FILE_ID);
                        String fileKey = existingFile.getFileKey();
                        when(fileRepository.findById(VALID_FILE_ID)).thenReturn(Optional.of(existingFile));
                        doThrow(new RuntimeException("Deletion failed")).when(backblazeB2Service).deleteFile(fileKey);

                        // THEN
                        Assertions.assertThrows(
                                        FileDeletionException.class,
                                        () -> fileService.deleteFile(new User(), VALID_FILE_ID));

                }
        }

}
