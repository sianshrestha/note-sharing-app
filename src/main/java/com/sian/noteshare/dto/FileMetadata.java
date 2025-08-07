package com.sian.noteshare.dto;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class FileMetadata {
    private String originalFileName;
    private String storedFileName;
    private String filePath;
    private String fileType;
    private long fileSize;
}