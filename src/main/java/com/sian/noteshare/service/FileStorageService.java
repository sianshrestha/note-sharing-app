package com.sian.noteshare.service;


import com.sian.noteshare.dto.FileMetadata;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
public class FileStorageService {

    @Value("${file.upload-dir}")
    private String uploadDir;

    private Path fileStorageLocation;

    @PostConstruct
    public void init() {
        this.fileStorageLocation=Paths.get(uploadDir).toAbsolutePath().normalize();
        try {
            Files.createDirectories(fileStorageLocation);
        } catch (IOException e) {
            throw new RuntimeException("Could not create upload directory: " + uploadDir, e);
        }
    }

    public String storeFile(MultipartFile file) {
        String originalFileName = file.getOriginalFilename();
        String fileExtension = "";

        int dotIndex = originalFileName.lastIndexOf('.');
        if (dotIndex >= 0) {
            fileExtension = originalFileName.substring(dotIndex);
        }

        String storedFileName = UUID.randomUUID().toString() + "." + fileExtension;

        try {
            Path targetLocation = fileStorageLocation.resolve(storedFileName);
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);
            return storedFileName;
        } catch (IOException e) {
            throw new RuntimeException("Could not store file " + originalFileName + ". Please try again!", e);
        }
    }

    public Path loadFileAsPath(String storedFileName) {
        return fileStorageLocation.resolve(storedFileName).normalize();
    }
}
