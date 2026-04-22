package com.sian.noteshare.service;

import com.sian.noteshare.dto.UserProfileResponse;
import com.sian.noteshare.dto.UserProfileResponse.NoteSummary;
import com.sian.noteshare.dto.UserProfileUpdateRequest;
import com.sian.noteshare.entity.Bookmark;
import com.sian.noteshare.entity.Note;
import com.sian.noteshare.entity.User;
import com.sian.noteshare.exception.ResourceNotFoundException;
import com.sian.noteshare.exception.UserAlreadyExistsException;
import com.sian.noteshare.repository.BookmarkRepository;
import com.sian.noteshare.repository.NoteRepository;
import com.sian.noteshare.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service class for managing and retrieving user profile data.
 * Aggregates information regarding user details, uploaded notes, and bookmarked notes.
 */
@Service
@RequiredArgsConstructor
public class UserProfileService {

    private final NoteRepository noteRepository;
    private final UserRepository userRepository;
    private final BookmarkRepository bookmarkRepository;

    /**
     * Retrieves the profile details of a user, including their uploaded and bookmarked notes.
     *
     * @param username The username of the user whose profile is being requested.
     * @return UserProfileResponse containing aggregated user details.
     * @throws ResourceNotFoundException if the user does not exist.
     */
    public UserProfileResponse getUserProfile(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Username not found: " + username));

        List<NoteSummary> uploadedNotes = noteRepository.findByUploadedBy(user).stream()
                .map(this::mapToNoteSummary)
                .collect(Collectors.toList());

        List<NoteSummary> bookmarkedNotes = bookmarkRepository.findByUser(user).stream()
                .map(Bookmark::getNote)
                .map(this::mapToNoteSummary)
                .collect(Collectors.toList());

        return UserProfileResponse.builder()
                .username(user.getUsername())
                .email(user.getEmail())
                .registeredAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .uploadedNotes(uploadedNotes)
                .bookmarkedNotes(bookmarkedNotes)
                .build();
    }

    /**
     * Maps a Note entity to a lightweight NoteSummary DTO for profile views.
     *
     * @param note The Note entity to map.
     * @return NoteSummary DTO.
     */
    public NoteSummary mapToNoteSummary(Note note) {
        return new NoteSummary(
                note.getId(),
                note.getTitle(),
                note.getSubject(),
                note.getUploadedAt()
        );
    }

    /**
     * Updates the currently authenticated user's profile information (username or email).
     *
     * @param authentication The Spring Security authentication token containing the current username.
     * @param request        The update request containing new profile data.
     * @throws ResourceNotFoundException  if the current user cannot be found in the database.
     * @throws UserAlreadyExistsException if the new username or email is already taken.
     */
    public void updateUserProfile(Authentication authentication, UserProfileUpdateRequest request) {
        String currentUsername = authentication.getName();
        User user = userRepository.findByUsername(currentUsername)
                .orElseThrow(() -> new ResourceNotFoundException("Username not found: " + currentUsername));

        if (!user.getUsername().equals(request.getUsername()) && request.getUsername() != null) {
            if (userRepository.findByUsername(request.getUsername()).isPresent()) {
                throw new UserAlreadyExistsException("Username already taken");
            }
            user.setUsername(request.getUsername());
        }

        if (!user.getEmail().equals(request.getEmail()) && request.getEmail() != null) {
            if (userRepository.findByEmail(request.getEmail()).isPresent()) {
                throw new UserAlreadyExistsException("Email already exists");
            }
            user.setEmail(request.getEmail());
        }

        userRepository.save(user);
    }
}