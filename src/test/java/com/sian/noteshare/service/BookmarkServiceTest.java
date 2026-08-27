package com.sian.noteshare.service;

import com.sian.noteshare.dto.BookmarkResponse;
import com.sian.noteshare.entity.Bookmark;
import com.sian.noteshare.entity.Note;
import com.sian.noteshare.entity.User;
import com.sian.noteshare.repository.BookmarkRepository;
import com.sian.noteshare.repository.NoteRepository;
import com.sian.noteshare.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for the BookmarkService.
 * Validates the creation and prevention of duplicate bookmarks.
 */
@ExtendWith(MockitoExtension.class)
class BookmarkServiceTest {

    @Mock private BookmarkRepository bookmarkRepository;
    @Mock private NoteRepository noteRepository;
    @Mock private UserRepository userRepository;
    @Mock private FileStorageService fileStorageService;

    @InjectMocks private BookmarkService bookmarkService;

    private User testUser;
    private Note testNote;

    @BeforeEach
    void setUp() {
        testUser = User.builder().id(1L).username("testuser").build();
        testNote = Note.builder().id(1L).title("Test Note").uploadedBy(testUser).storedFileName("file.pdf").build();
    }

    /**
     * Tests successfully bookmarking a note.
     */
    @Test
    void addBookmark_ShouldSaveBookmark_WhenSuccess() {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(noteRepository.findById(1L)).thenReturn(Optional.of(testNote));
        when(bookmarkRepository.findByUserAndNote(testUser, testNote)).thenReturn(Optional.empty());

        Bookmark savedBookmark = Bookmark.builder().id(1L).user(testUser).note(testNote).build();
        when(bookmarkRepository.save(any(Bookmark.class))).thenReturn(savedBookmark);
        when(fileStorageService.generatePresignedUrl(any())).thenReturn("http://s3-url");

        BookmarkResponse response = bookmarkService.addBookmark(1L, "testuser");

        assertNotNull(response);
        assertEquals(1L, response.getNoteId());
        verify(bookmarkRepository, times(1)).save(any(Bookmark.class));
    }

    /**
     * Tests that attempting to bookmark the same note twice throws an IllegalStateException.
     */
    @Test
    void addBookmark_ShouldThrowException_WhenAlreadyExists() {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(noteRepository.findById(1L)).thenReturn(Optional.of(testNote));
        when(bookmarkRepository.findByUserAndNote(testUser, testNote)).thenReturn(Optional.of(new Bookmark()));

        assertThrows(IllegalStateException.class, () -> bookmarkService.addBookmark(1L, "testuser"));
        verify(bookmarkRepository, never()).save(any());
    }
}