package com.openclassrooms.datashare.service;

import org.springframework.stereotype.Service;

import com.openclassrooms.datashare.entities.FileData;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor
public class FileService {

    public String uploadFile(FileData fileData) {
        System.out.println("Received file data: " + fileData);
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'uploadFile'");
    }

}
