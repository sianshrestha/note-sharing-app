package com.sian.noteshare.dto;

import lombok.*;

import java.time.LocalDateTime;

@Data
@Builder
public class NoteResponse {
    private Long id;
    private String title;
    private String subject;
    private String description;
    private String originalFileName;
    private String fileType;
    private Long fileSize;
    private String downloadUrl;
    private LocalDateTime uploadedAt;
    private String uploadedByUsername;
}
