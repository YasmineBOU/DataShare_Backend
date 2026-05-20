package com.openclassrooms.datashare.mapper;

import com.openclassrooms.datashare.dto.FileUploadDTO;
import com.openclassrooms.datashare.entities.FileData;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mapstruct.factory.Mappers;
import org.springframework.mock.web.MockMultipartFile;

import static org.junit.jupiter.api.Assertions.*;

import java.util.stream.Stream;

public class FileDtoMapperTest {

    private final FileDtoMapper mapper = Mappers.getMapper(FileDtoMapper.class);
    private static final String FILENAME = "document.pdf";
    private static final int FILE_SIZE = 1024;
    private static final String FILE_TYPE = "application/pdf";
    private static final String HASH = "abc123";

    static Stream<Arguments> provideEmailAndPasswords() {
        return Stream.of(
                // email = null, password = null
                Arguments.of(null, null),
                // email = any, password = null
                Arguments.of("user@example.com", null),
                // email = null, password = any
                Arguments.of(null, "password1"),
                // email = any, password = any
                Arguments.of("user@example.com", "password2"));
    }

    @ParameterizedTest
    @MethodSource("provideEmailAndPasswords")
    void test_toEntity_fromFileUploadDTO_mapsBasicFields(String email, String filePassword) {
        // GIVEN
        FileUploadDTO dto = new FileUploadDTO();
        dto.setFile(new MockMultipartFile("file", FILENAME, FILE_TYPE, new byte[FILE_SIZE]));
        dto.setEmail(email);
        dto.setFilePassword(filePassword);
        dto.setFilename(FILENAME);
        dto.setFileSize(FILE_SIZE);
        dto.setFileType(FILE_TYPE);
        dto.setHash(HASH);

        // WHEN
        FileData fileData = mapper.toEntity(dto);

        // THEN
        assertNotNull(fileData);
        assertEquals(email, fileData.getEmail());
        assertEquals(filePassword, fileData.getFilePassword());
        assertEquals(FILENAME, fileData.getFilename());
        assertEquals(FILE_SIZE, fileData.getFileSize());
        assertEquals(FILE_TYPE, fileData.getFileType());
        assertEquals(HASH, fileData.getHash());

        // Ignored by mapper
        assertNull(fileData.getId());
        assertNull(fileData.getUpdatedAt());
        assertNull(fileData.getCreatedAt());
        assertNull(fileData.getExpirationDate());
        assertNull(fileData.getFileLink());
        assertNull(fileData.getFileKey());
        assertNull(fileData.getFileToken());
        assertFalse(fileData.isDeleted());
    }
}
