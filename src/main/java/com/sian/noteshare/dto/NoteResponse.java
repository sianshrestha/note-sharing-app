package com.sian.noteshare.dto;

import lombok.*;

import java.time.Instant;

/**
 * Data Transfer Object for returning comprehensive details of a Note, including its S3 download URL.
 */
@Data
@Builder
public class NoteResponse {
    private Long id;
    private String title;
    private String subject;
    private String description;
    private String originalFileName;
    private String storedFileName;
    private String fileType;
    private Long fileSize;
    private String downloadUrl;
    private Instant uploadedAt;
    private String uploadedBy;
}
