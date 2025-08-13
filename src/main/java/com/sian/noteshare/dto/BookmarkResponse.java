package com.sian.noteshare.dto;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@Builder
public class BookmarkResponse {
    private Long id;
    private Long noteId;
    private String noteTitle;
    private String noteSubject;
    private String noteUploadedBy;
    private String downloadUrl;
    private Instant createdAt;
}
