package com.sian.noteshare.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.Set;

/**
 * Entity representing a document/note uploaded by a user.
 * Stores metadata such as title, subject, and physical file references in AWS S3.
 */
@Entity
@Table(name = "notes")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Note {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;
    private String subject;
    private String description;

    /**
     * The original name of the file when uploaded by the user.
     */
    private String originalFileName;

    /**
     * The unique UUID filename used to store the object in AWS S3.
     */
    private String storedFileName;

    private String fileType;
    private Long fileSize;

    private Instant uploadedAt;

    /**
     * The user who originally uploaded this note.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User uploadedBy;

    /**
     * Bookmarks associated with this note across all users.
     */
    @OneToMany(mappedBy = "note", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Bookmark> bookmarks;
}