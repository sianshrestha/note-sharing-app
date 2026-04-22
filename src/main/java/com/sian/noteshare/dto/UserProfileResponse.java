package com.sian.noteshare.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;

/**
 * Data Transfer Object (DTO) used to securely send user profile details
 * to the client, including lightweight summaries of their uploaded and bookmarked notes.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserProfileResponse {

    private String username;
    private String email;
    private Instant registeredAt;
    private Instant updatedAt;

    /**
     * List of notes uploaded by the user.
     */
    private List<NoteSummary> uploadedNotes;

    /**
     * List of notes bookmarked by the user.
     */
    private List<NoteSummary> bookmarkedNotes;

    /**
     * A nested static class representing a lightweight summary of a Note.
     * Used to prevent sending full file details and S3 URLs when just listing profiles.
     */
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