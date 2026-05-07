package com.openclassrooms.datashare.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import com.openclassrooms.datashare.dto.FileUploadDTO;
import com.openclassrooms.datashare.entities.FileData;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface FileDtoMapper {
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "expirationDate", ignore = true)
    @Mapping(target = "fileLink", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    FileData toEntity(FileUploadDTO fileUploadDTO);
}