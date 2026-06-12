package com.openclassrooms.datashare.controller;

import com.openclassrooms.datashare.dto.FileDownloadDTO;
import com.openclassrooms.datashare.dto.FileInfoDTO;
import com.openclassrooms.datashare.dto.FileUploadDTO;
import com.openclassrooms.datashare.entities.User;
import com.openclassrooms.datashare.handler.FileExceptionHandler;
import com.openclassrooms.datashare.handler.exceptions.*;
import com.openclassrooms.datashare.mapper.FileDtoMapper;
import com.openclassrooms.datashare.repository.UserRepository;
import com.openclassrooms.datashare.service.FileService;

import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.context.annotation.Import;
import com.openclassrooms.datashare.configuration.security.JwtAuthenticationFilter;

import tools.jackson.databind.ObjectMapper;

@WebMvcTest(FileController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(FileExceptionHandler.class)
public class FileControllerTest {

    private String URL;
    private static final int EXPIRATION_DAYS = 7;
    private static final Map<String, String> URLS_BY_METHOD = Map.of(
            "upload", "/api/files/upload",
            "download", "/api/files/download",
            "list", "/api/files/list",
            "info", "/api/files/info",
            "delete", "/api/files/delete/{id}");

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private FileService fileService;

    @MockitoBean
    private UserRepository userRepository;

    @MockitoBean
    private FileDtoMapper fileDtoMapper;

    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private Map getResponseBodyAsMap(MvcResult mvcResult) throws Exception {
        String responseContent = mvcResult.getResponse().getContentAsString();
        return objectMapper.readValue(responseContent, Map.class);
    }

    @Nested
    @Tag("uploadFile")
    @DisplayName("Tests for upload endpoint")
    class UploadFileTests {

        private static String FILE_HASH = "calculated-file-hash-123";
        private static String FILENAME = "test.pdf";
        private static long FILE_SIZE = 1024L;
        private static String FILE_TYPE = MediaType.APPLICATION_PDF_VALUE;

        MockMultipartFile FILE = new MockMultipartFile(
                "file",
                FILENAME,
                FILE_TYPE,
                "PDF content".getBytes());

        @BeforeEach
        public void setUp() {
            URL = URLS_BY_METHOD.get("upload");
        }

        @Test
        @DisplayName("Given a valid file upload request, when uploadFile is called, then file is uploaded and success message is returned")
        public void test_upload_file_with_valid_request_should_return_success() throws Exception {

            FileUploadDTO fileUploadDTO = new FileUploadDTO();
            fileUploadDTO.setFile(FILE);
            fileUploadDTO.setFilename(FILENAME);
            fileUploadDTO.setFileSize(FILE_SIZE);
            fileUploadDTO.setFileType(FILE_TYPE);
            fileUploadDTO.setHash(FILE_HASH);
            fileUploadDTO.setExpirationDays(EXPIRATION_DAYS);

            String fileToken = "generated-token-123";
            when(fileService.uploadFile(any(), any(), anyInt())).thenReturn(fileToken);

            // WHEN & THEN
            mockMvc.perform(MockMvcRequestBuilders.multipart(URL)
                    .file(FILE)
                    .flashAttr("fileUploadDTO", fileUploadDTO)
                    .contentType(MediaType.MULTIPART_FORM_DATA_VALUE))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message").value("File uploaded successfully !"))
                    .andExpect(jsonPath("$.fileToken").value(fileToken));

            verify(fileService, times(1)).uploadFile(any(), any(), anyInt());
        }

        @Test
        @DisplayName("Given no file in the request, when uploadFile is called, then return Bad Request")
        public void test_uploadFile_with_no_file_should_return_bad_request() throws Exception {
            // WHEN & THEN
            mockMvc.perform(MockMvcRequestBuilders.multipart(URL)
                    .param("expirationDays", String.valueOf(EXPIRATION_DAYS)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Given a missing expirationDays parameter, when uploadFile is called, then return Bad Request")
        public void test_uploadFile_with_missing_expirationDays_should_return_bad_request() throws Exception {
            // GIVEN
            FileUploadDTO fileUploadDTO = new FileUploadDTO();
            fileUploadDTO.setFile(FILE);
            fileUploadDTO.setFilename(FILENAME);
            fileUploadDTO.setFileSize(FILE_SIZE);
            fileUploadDTO.setFileType(FILE_TYPE);

            // WHEN & THEN
            mockMvc.perform(MockMvcRequestBuilders.multipart(URL)
                    .file(FILE)
                    .flashAttr("fileUploadDTO", fileUploadDTO))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Given a file with a password that doesn't meet the password policy, when uploadFile is called, then return Bad Request")
        public void test_uploadFile_with_invalid_password_returns_bad_request() throws Exception {
            // GIVEN
            FileUploadDTO fileUploadDTO = new FileUploadDTO();
            fileUploadDTO.setFile(FILE);
            fileUploadDTO.setFilename(FILENAME);
            fileUploadDTO.setFileSize(FILE_SIZE);
            fileUploadDTO.setFileType(FILE_TYPE);
            fileUploadDTO.setHash(FILE_HASH);
            fileUploadDTO.setExpirationDays(EXPIRATION_DAYS);
            fileUploadDTO.setFilePassword("123"); // Invalid password (too short)

            // WHEN & THEN
            mockMvc.perform(MockMvcRequestBuilders.multipart(URL)
                    .file(FILE)
                    .flashAttr("fileUploadDTO", fileUploadDTO))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Given a file with a password that meets the password policy, when uploadFile is called, then return success")
        public void test_uploadFile_with_valid_password_returns_success() throws Exception {
            // GIVEN
            FileUploadDTO fileUploadDTO = new FileUploadDTO();
            fileUploadDTO.setFile(FILE);
            fileUploadDTO.setFilename(FILENAME);
            fileUploadDTO.setFileSize(FILE_SIZE);
            fileUploadDTO.setFileType(FILE_TYPE);
            fileUploadDTO.setHash(FILE_HASH);
            fileUploadDTO.setExpirationDays(EXPIRATION_DAYS);
            fileUploadDTO.setFilePassword("password"); // Valid password

            String fileToken = "generated-token-123";
            when(fileService.uploadFile(any(), any(), anyInt())).thenReturn(fileToken);

            // WHEN & THEN
            mockMvc.perform(MockMvcRequestBuilders.multipart(URL)
                    .file(FILE)
                    .flashAttr("fileUploadDTO", fileUploadDTO))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message").value("File uploaded successfully !"))
                    .andExpect(jsonPath("$.fileToken").value(fileToken));

            verify(fileService, times(1)).uploadFile(any(), any(), anyInt());
        }
    }

    @Nested
    @Tag("downloadFile")
    @DisplayName("Tests for download endpoint")
    class DownloadFileTests {

        @BeforeEach
        public void setUp() {
            URL = URLS_BY_METHOD.get("download");
        }

        @Test
        @DisplayName("Given a valid file download request, when downloadFile is called, then return file link")
        public void test_download_file_with_valid_request_should_return_file_link() throws Exception {
            // GIVEN
            FileDownloadDTO fileDownloadDTO = new FileDownloadDTO();
            fileDownloadDTO.setId(1L);
            // fileDownloadDTO.setFilePassword("password");

            String fileLink = "file-link-123";
            when(fileService.downloadFile(anyLong(), any())).thenReturn(fileLink);

            // WHEN & THEN
            mockMvc.perform(MockMvcRequestBuilders.post(URL)
                    .content(objectMapper.writeValueAsString(fileDownloadDTO))
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message").value("File link retrieved successfully !"))
                    .andExpect(jsonPath("$.fileLink").value(fileLink));
        }

        @Test
        @DisplayName("Given a filePassword provided, when downloadFile is called, then forward password to service")
        public void test_download_file_with_password_should_forward_password() throws Exception {
            // GIVEN
            FileDownloadDTO fileDownloadDTO = new FileDownloadDTO();
            fileDownloadDTO.setId(2L);
            fileDownloadDTO.setFilePassword("secret");

            String fileLink = "file-link-xyz";
            when(fileService.downloadFile(anyLong(), any())).thenReturn(fileLink);

            // WHEN & THEN
            mockMvc.perform(MockMvcRequestBuilders.post(URL)
                    .content(objectMapper.writeValueAsString(fileDownloadDTO))
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.fileLink").value(fileLink));

            verify(fileService, times(1)).downloadFile(eq(2L), eq("secret"));
        }

        @Test
        @DisplayName("Given no ID, when downloadFile is called, then return FileNotFound")
        public void test_download_file_with_no_id_should_return_FileNotFound() throws Exception {
            // GIVEN
            FileDownloadDTO fileDownloadDTO = new FileDownloadDTO();

            // WHEN & THEN
            mockMvc.perform(MockMvcRequestBuilders.post(URL)
                    .content(objectMapper.writeValueAsString(fileDownloadDTO))
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Given a non-existing file ID, when downloadFile is called, then return FileNotFound")
        public void test_download_file_with_non_existing_id_should_return_FileNotFound() throws Exception {
            // GIVEN
            FileDownloadDTO fileDownloadDTO = new FileDownloadDTO();
            fileDownloadDTO.setId(999L);

            doThrow(new FileNotFoundException("File not found")).when(fileService).downloadFile(anyLong(), any());

            // WHEN & THEN
            mockMvc.perform(MockMvcRequestBuilders.post(URL)
                    .content(objectMapper.writeValueAsString(fileDownloadDTO))
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.message").value("File not found"));
        }
    }

    @Nested
    @Tag("listFile")
    @DisplayName("Tests for list endpoint")
    class ListFileTests {

        private static final String EMAIL = "user@example.com";

        @BeforeEach
        public void setUp() {
            URL = URLS_BY_METHOD.get("list");
        }

        @Test
        @DisplayName("Given an authenticated user and a valid email, when listFiles is called, then return success response")
        @WithMockUser
        public void test_listFiles_withValidRequest_shouldReturnSuccessResponse() throws Exception {
            // GIVEN
            User authenticatedUser = new User();
            authenticatedUser.setEmail(EMAIL);

            List<FileInfoDTO> files = List.of(
                    new FileInfoDTO(1L, "file1.pdf", "fileToken1", 1024L, null, null, false),
                    new FileInfoDTO(2L, "file2.pdf", "fileToken2", 2048L, null, null, false));

            when(fileService.listFiles(authenticatedUser, EMAIL)).thenReturn(files);

            // WHEN & THEN
            mockMvc.perform(MockMvcRequestBuilders.get(URL)
                    .param("email", EMAIL)
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message").value("Files retrieved successfully !"))
                    .andExpect(jsonPath("$.files").isArray())
                    .andExpect(jsonPath("$.files.length()").value(2))
                    .andExpect(jsonPath("$.files[0].filename").value("file1.pdf"))
                    .andExpect(jsonPath("$.files[1].filename").value("file2.pdf"));
        }

        @Test
        @DisplayName("Given a missing email parameter, when listFiles is called, then return Bad Request")
        @WithMockUser
        public void test_listFiles_with_missing_email_should_return_bad_request() throws Exception {
            // WHEN & THEN
            mockMvc.perform(MockMvcRequestBuilders.get(URL)
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Given a non-existing email, when listFiles is called, then return empty file list")
        @WithMockUser
        public void test_listFiles_with_non_existing_email_should_return_empty_file_list() throws Exception {
            // GIVEN
            String email = "non-existing-email";
            when(fileService.listFiles(any(), anyString())).thenReturn(List.of());

            // WHEN & THEN
            mockMvc.perform(MockMvcRequestBuilders.get(URL)
                    .param("email", email)
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message").value("Files retrieved successfully !"))
                    .andExpect(jsonPath("$.files").isArray())
                    .andExpect(jsonPath("$.files.length()").value(0));
        }
    }

    @Nested
    @Tag("getFileInfo")
    @DisplayName("Tests for info endpoint")
    class GetFileInfoTests {

        @BeforeEach
        public void setUp() {
            URL = URLS_BY_METHOD.get("info");
        }

        @Test
        @DisplayName("Given a valid fileToken, when getFileInfo is called, then return file info")
        public void test_getFileInfo_with_valid_fileToken_should_return_file_info() throws Exception {
            // GIVEN
            String fileToken = "valid-file-token";
            FileInfoDTO fileInfo = new FileInfoDTO(
                    1L,
                    "file.pdf",
                    fileToken,
                    1024L,
                    null,
                    null,
                    false);

            when(fileService.getFileInfoByFileToken(fileToken)).thenReturn(fileInfo);

            // WHEN & THEN
            mockMvc.perform(MockMvcRequestBuilders.get(URL)
                    .param("fileToken", fileToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(1L))
                    .andExpect(jsonPath("$.filename").value("file.pdf"))
                    .andExpect(jsonPath("$.fileToken").value(fileToken))
                    .andExpect(jsonPath("$.fileSize").value(1024L));
        }

        @Test
        @DisplayName("Given a missing fileToken, when getFileInfo is called, then return Bad Request")
        public void test_getFileInfo_with_missing_fileToken_should_return_BadRequest() throws Exception {
            // WHEN & THEN
            mockMvc.perform(MockMvcRequestBuilders.get(URL)
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Given an invalid fileToken, when getFileInfo is called, then return FileNotFoundException")
        public void test_getFileInfo_with_invalid_fileToken_should_return_FileNotFoundException() throws Exception {
            // GIVEN
            String invalidFileToken = "invalid-file-token";
            doThrow(new FileNotFoundException("No file found for file token: invalid-file-token")).when(fileService)
                    .getFileInfoByFileToken(invalidFileToken);

            // WHEN & THEN
            mockMvc.perform(MockMvcRequestBuilders.get(URL)
                    .param("fileToken", invalidFileToken))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @Tag("deleteFile")
    @DisplayName("Tests for delete endpoint")
    class DeleteFileTests {

        @BeforeEach
        public void setUp() {
            URL = URLS_BY_METHOD.get("delete");
        }

        @Test
        @DisplayName("Given a valid file ID, when deleteFile is called, then return success message")
        public void test_deleteFile_with_valid_id_should_return_success_message() throws Exception {
            // GIVEN
            Long fileId = 1L;

            // WHEN & THEN
            mockMvc.perform(MockMvcRequestBuilders.delete(URL, fileId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message").value("File deleted successfully !"));

            verify(fileService, times(1)).deleteFile(any(), anyLong());
        }

        @Test
        @DisplayName("Given a non-existing file ID, when deleteFile is called, then return FileNotFoundException")
        public void test_deleteFile_with_non_existing_id_should_return_FileNotFoundException() throws Exception {
            // GIVEN
            Long nonExistingFileId = 999L;
            doThrow(new FileNotFoundException("File not found")).when(fileService).deleteFile(any(), anyLong());

            // WHEN & THEN
            mockMvc.perform(MockMvcRequestBuilders.delete(URL, nonExistingFileId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isNotFound());
        }
    }

}
