package com.sian.noteshare.service;

import com.sian.noteshare.dto.UserProfileResponse;
import com.sian.noteshare.dto.UserProfileResponse.NoteSummary;
import com.sian.noteshare.entity.Bookmark;
import com.sian.noteshare.entity.Note;
import com.sian.noteshare.entity.User;
import com.sian.noteshare.repository.BookmarkRepository;
import com.sian.noteshare.repository.NoteRepository;
import com.sian.noteshare.repository.UserRepository;
import lombok.RequiredArgsConstructor;
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
                .orElseThrow(() -> new RuntimeException("Username not found: " + username));

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
}
