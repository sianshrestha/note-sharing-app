package com.sian.noteshare.service;

import com.sian.noteshare.dto.BookmarkResponse;
import com.sian.noteshare.entity.Bookmark;
import com.sian.noteshare.entity.Note;
import com.sian.noteshare.entity.User;
import com.sian.noteshare.exception.ResourceNotFoundException;
import com.sian.noteshare.repository.BookmarkRepository;
import com.sian.noteshare.repository.NoteRepository;
import com.sian.noteshare.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BookmarkService {
    private final BookmarkRepository bookmarkRepository;
    private final NoteRepository noteRepository;
    private final UserRepository userRepository;
    private final FileStorageService fileStorageService;

    @Transactional
    public BookmarkResponse addBookmark(Long noteId, String username) {

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found with username: " + username));

        Note note = noteRepository.findById(noteId)
                .orElseThrow(() -> new RuntimeException("Note not found with id: " + noteId));

        bookmarkRepository.findByUserAndNote(user, note)
                .ifPresent(b -> { throw new IllegalStateException("Bookmark already exists"); });

        Bookmark bookmark = Bookmark.builder()
                .user(user)
                .note(note)
                .build();

        Bookmark SavedBookmark = bookmarkRepository.save(bookmark);
        return mapToResponse(SavedBookmark);
    }

    @Transactional
    public void removeBookmark(Long noteId, String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found with username: " + username));

        Note note = noteRepository.findById(noteId)
                .orElseThrow(() -> new RuntimeException("Note not found with id: " + noteId));

        Bookmark bookmark = bookmarkRepository.findByUserAndNote(user, note)
                .orElseThrow(() -> new RuntimeException("Bookmark not found for user and note"));

        bookmarkRepository.delete(bookmark);
    }

    public List<BookmarkResponse> listBookmarks(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));

        return bookmarkRepository.findByUser(user).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private BookmarkResponse mapToResponse(Bookmark bookmark) {
        return BookmarkResponse.builder()
                .id(bookmark.getId())
                .noteId(bookmark.getNote().getId())
                .noteTitle(bookmark.getNote().getTitle())
                .noteSubject(bookmark.getNote().getSubject())
                .noteUploadedBy(bookmark.getNote().getUploadedBy().getUsername())
                .downloadUrl(fileStorageService.generatePresignedUrl(bookmark.getNote().getStoredFileName()))
                .createdAt(bookmark.getCreatedAt())
                .build();
    }

}
