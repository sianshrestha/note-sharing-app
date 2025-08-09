package com.sian.noteshare.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.springframework.web.multipart.MultipartFile;

@Data
public class NoteUploadRequest {
    @NotBlank(message = "Title is required")
    private String title;
    private String subject;
    private String description;

    @NotNull(message = "File is required")
    private MultipartFile file;
}