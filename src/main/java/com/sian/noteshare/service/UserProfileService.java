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

@Service
@RequiredArgsConstructor
public class UserProfileService {

    private final NoteRepository noteRepository;
    private final UserRepository userRepository;
    private final BookmarkRepository bookmarkRepository;

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

    public NoteSummary mapToNoteSummary(Note note) {
        return new NoteSummary(
                note.getId(),
                note.getTitle(),
                note.getSubject(),
                note.getUploadedAt()
        );
    }

    public User updateUserProfile(Authentication authentication, UserProfileUpdateRequest request) {
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

        return userRepository.save(user);
    }
}
