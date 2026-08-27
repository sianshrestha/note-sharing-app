package com.sian.noteshare.service;

import com.sian.noteshare.dto.NoteResponse;
import com.sian.noteshare.dto.NoteUpdateRequest;
import com.sian.noteshare.dto.NoteUploadRequest;
import com.sian.noteshare.entity.Note;
import com.sian.noteshare.entity.User;
import com.sian.noteshare.exception.FileStorageException;
import com.sian.noteshare.repository.NoteRepository;
import com.sian.noteshare.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for the NoteService business logic.
 * Mocks all external dependencies to test the service layer in isolation.
 */
@ExtendWith(MockitoExtension.class)
class NoteServiceTest {

    @Mock private NoteRepository noteRepository;
    @Mock private UserRepository userRepository;
    @Mock private FileStorageService fileStorageService;
    @Mock private EmailService emailService;

    @InjectMocks private NoteService noteService;

    private User testUser;
    private Note testNote;

    @BeforeEach
    void setUp() {
        testUser = User.builder().username("testuser").email("test@example.com").build();
        testNote = Note.builder().id(1L).title("My Note").uploadedBy(testUser).storedFileName("uuid-file.pdf").build();
    }

    /**
     * Mocks the Spring SecurityContext for the current thread.
     */
    private void mockSecurityContext(String username, boolean isAdmin) {
        var authorities = isAdmin
                ? List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))
                : List.of(new SimpleGrantedAuthority("ROLE_USER"));

        TestingAuthenticationToken auth = new TestingAuthenticationToken(username, null, authorities);
        auth.setAuthenticated(true);

        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(auth);
        SecurityContextHolder.setContext(context);
    }

    /**
     * Tests that a valid note upload successfully stores the file, saves metadata, and sends an email.
     */
    @Test
    void uploadNote_ShouldReturnResponse_WhenValid() {
        mockSecurityContext("testuser", false);
        MockMultipartFile file = new MockMultipartFile("file", "test.pdf", "application/pdf", "content".getBytes());
        NoteUploadRequest request = new NoteUploadRequest();
        request.setTitle("Test Note");
        request.setFile(file);

        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(fileStorageService.storeFile(any())).thenReturn("uuid-test.pdf");
        when(noteRepository.save(any(Note.class))).thenReturn(testNote);
        when(fileStorageService.generatePresignedUrl(any())).thenReturn("http://s3-url");

        NoteResponse response = noteService.uploadNote(request);

        assertNotNull(response);
        verify(emailService, times(1)).sendUploadConfirmation(any(), any());
        verify(fileStorageService, times(1)).storeFile(any());
    }

    /**
     * Tests that an empty file triggers a FileStorageException.
     */
    @Test
    void uploadNote_ShouldThrowException_WhenFileEmpty() {
        MockMultipartFile file = new MockMultipartFile("file", "test.pdf", "application/pdf", new byte[0]);
        NoteUploadRequest request = new NoteUploadRequest();
        request.setFile(file);

        assertThrows(FileStorageException.class, () -> noteService.uploadNote(request));
    }

    /**
     * Tests that a file with an unsupported MIME type triggers a FileStorageException.
     */
    @Test
    void uploadNote_ShouldThrowException_WhenInvalidType() {
        MockMultipartFile file = new MockMultipartFile("file", "test.exe", "application/octet-stream", "content".getBytes());
        NoteUploadRequest request = new NoteUploadRequest();
        request.setFile(file);

        assertThrows(FileStorageException.class, () -> noteService.uploadNote(request));
    }

    /**
     * Tests that a user cannot update a note owned by someone else unless they are an admin.
     */
    @Test
    void updateNote_ShouldThrowSecurityException_WhenNotOwner() {
        mockSecurityContext("hacker", false);
        when(noteRepository.findById(1L)).thenReturn(Optional.of(testNote));

        NoteUpdateRequest request = new NoteUpdateRequest();

        assertThrows(SecurityException.class, () -> noteService.updateNote(1L, request, "hacker"));
    }

    /**
     * Tests that deleting a note successfully removes the file from S3 and the record from the database.
     */
    @Test
    void deleteNote_ShouldCallDeleteFileAndRepository_WhenSuccess() {
        mockSecurityContext("testuser", false);
        when(noteRepository.findById(1L)).thenReturn(Optional.of(testNote));

        noteService.deleteNote(1L, "testuser");

        verify(fileStorageService, times(1)).deleteFile("uuid-file.pdf");
        verify(noteRepository, times(1)).deleteById(1L);
    }
}
