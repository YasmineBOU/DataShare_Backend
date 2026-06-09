package com.openclassrooms.datashare.mapper;

import com.openclassrooms.datashare.dto.FileUploadDTO;
import com.openclassrooms.datashare.entities.FileData;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

/**
 * Mapper interface for converting between {@link FileUploadDTO} and
 * {@link FileData} entities.
 * This mapper uses MapStruct to automatically generate the implementation for
 * mapping data transfer objects
 * to entity objects, ensuring type safety and reducing boilerplate code.
 *
 * <p>
 * Key functionalities include:
 * <ul>
 * <li>Mapping a {@link FileUploadDTO} to a {@link FileData} entity.</li>
 * <li>Ignoring certain fields (e.g., ID, timestamps, file links) during the
 * mapping process.</li>
 * <li>Setting default values for fields (e.g., marking files as not deleted by
 * default).</li>
 * </ul>
 *
 * @see FileUploadDTO
 * @see FileData
 * @see Mapper
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface FileDtoMapper {
    /**
     * Maps a {@link FileUploadDTO} to a {@link FileData} entity.
     * This method ignores the following fields in the entity:
     * <ul>
     * <li>ID</li>
     * <li>Creation timestamp</li>
     * <li>Expiration date</li>
     * <li>File link</li>
     * <li>Update timestamp</li>
     * <li>File key</li>
     * <li>File token</li>
     * </ul>
     * It also sets the following default values:
     * <ul>
     * <li><code>deleted</code> to <code>false</code></li>
     * </ul>
     *
     * @param fileUploadDTO The DTO containing the file upload data.
     * @return The mapped {@link FileData} entity.
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "expirationDate", ignore = true)
    @Mapping(target = "fileLink", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "fileKey", ignore = true)
    @Mapping(target = "fileToken", ignore = true)
    @Mapping(target = "deleted", constant = "false")
    FileData toEntity(FileUploadDTO fileUploadDTO);
}