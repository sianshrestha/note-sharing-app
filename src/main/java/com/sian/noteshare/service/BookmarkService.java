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

/**
 * Service class for managing user bookmarks.
 * Allows users to save notes to their profile for quick access.
 */
@Service
@RequiredArgsConstructor
public class BookmarkService {
    private final BookmarkRepository bookmarkRepository;
    private final NoteRepository noteRepository;
    private final UserRepository userRepository;
    private final FileStorageService fileStorageService;

    /**
     * Creates a new bookmark linking a user to a specific note.
     *
     * @param noteId The ID of the note to be bookmarked.
     * @param username The username of the user creating the bookmark.
     * @return BookmarkResponse representing the newly created bookmark.
     * @throws IllegalStateException if the note is already bookmarked by the user.
     * @throws RuntimeException if the user or note is not found.
     */
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

    /**
     * Removes an existing bookmark for a user.
     *
     * @param noteId The ID of the bookmarked note.
     * @param username The username of the user removing the bookmark.
     * @throws RuntimeException if the user, note, or bookmark relationship is not found.
     */
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

    /**
     * Retrieves all bookmarks saved by a specific user.
     *
     * @param username The username of the requesting user.
     * @return A list of BookmarkResponse DTOs.
     * @throws ResourceNotFoundException if the user is not found.
     */
    public List<BookmarkResponse> listBookmarks(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));

        return bookmarkRepository.findByUser(user).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Maps a Bookmark entity to a BookmarkResponse DTO, generating a presigned URL for the note.
     *
     * @param bookmark The Bookmark entity.
     * @return BookmarkResponse DTO.
     */
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