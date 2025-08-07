package com.sian.noteshare.dto;

import lombok.*;
import org.springframework.web.multipart.MultipartFile;

@Data
public class NoteUploadRequest {
    private String title;
    private String subject;
    private String description;
    private MultipartFile file;
}