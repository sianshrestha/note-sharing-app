package com.sian.noteshare.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserProfileResponse {
    private String username;
    private String email;
    private Instant registeredAt;
    private Instant updatedAt;
    private List<NoteSummary> uploadedNotes;
    private List<NoteSummary> bookmarkedNotes;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class NoteSummary {
        private Long id;
        private String title;
        private String subject;
        private Instant uploadedAt;
    }
}
