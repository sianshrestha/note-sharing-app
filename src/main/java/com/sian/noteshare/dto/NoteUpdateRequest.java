package com.sian.noteshare.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NoteUpdateRequest {
    private String title;
    private String subject;
    private String description;
    private MultipartFile file;
}