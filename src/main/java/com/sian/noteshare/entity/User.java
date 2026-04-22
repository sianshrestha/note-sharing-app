package com.sian.noteshare.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.Set;

/**
 * Represents a registered user within the Note Sharing Platform.
 * Maps to the "users" table in the database and stores authentication
 * details, roles, and relationships to bookmarked notes.
 */
@Entity
@Table(name="users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Unique display name for the user.
     */
    @Column(unique = true, nullable = false, length = 50)
    private String username;

    /**
     * BCrypt hashed password. Nullable to support OAuth2 users who do not have local passwords.
     */
    @Column(length = 255, nullable = true)
    private String password;

    /**
     * Unique email address used for login and notifications.
     */
    @Column(length = 100, unique = true, nullable = false)
    private String email;

    /**
     * Security roles assigned to the user (e.g., ROLE_USER, ROLE_ADMIN).
     */
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "user_roles", joinColumns = @JoinColumn(name = "user_id"))
    @Column(name = "role")
    private Set<String> roles;

    /**
     * Identifies the authentication method used by the user (Local, Google, or GitHub).
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AuthProvider provider;

    @CreationTimestamp
    @Column(updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    private Instant updatedAt;

    /**
     * The set of bookmarks saved by this user.
     */
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Bookmark> bookmarks;

    /**
     * Supported authentication providers.
     */
    public enum AuthProvider {
        LOCAL,
        GOOGLE,
        GITHUB
    }
}