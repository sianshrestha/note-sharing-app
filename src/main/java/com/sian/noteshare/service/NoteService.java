package com.sian.noteshare.service;

import com.sian.noteshare.dto.NoteResponse;
import com.sian.noteshare.dto.NoteUpdateRequest;
import com.sian.noteshare.dto.NoteUploadRequest;
import com.sian.noteshare.entity.Note;
import com.sian.noteshare.entity.User;
import com.sian.noteshare.exception.FileStorageException;
import com.sian.noteshare.exception.ResourceNotFoundException;
import com.sian.noteshare.repository.NoteRepository;
import com.sian.noteshare.repository.UserRepository;
import com.sian.noteshare.util.FileTypeValidator;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.Instant;

/**
 * Service class handling core business logic for Note management.
 * This includes uploading, updating, retrieving, and deleting notes,
 * as well as interacting with the FileStorageService.
 */
@Service
@RequiredArgsConstructor
public class NoteService {

    private final NoteRepository noteRepository;
    private final UserRepository userRepository;
    private final FileStorageService fileStorageService;
    private final EmailService emailService;

    /**
     * Validates and uploads a file to S3, then saves the note metadata in the database.
     *
     * @param request The note upload request containing the file and metadata.
     * @return NoteResponse containing the saved note details and download URL.
     * @throws FileStorageException if the file is empty or of an invalid type.
     * @throws ResourceNotFoundException if the authenticated user cannot be found.
     */
    public NoteResponse uploadNote(NoteUploadRequest request) {
        MultipartFile file = request.getFile();

        if (file == null || file.isEmpty()) {
            throw new FileStorageException("File is empty or missing.");
        }

        String contentType = file.getContentType();
        if (!FileTypeValidator.isAllowed(contentType)) {
            throw new FileStorageException("Invalid file type: " + contentType);
        }

        String storedFileName = fileStorageService.storeFile(file);
        String originalFileName = file.getOriginalFilename();

        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found."));

        Note note = Note.builder()
                .title(request.getTitle())
                .subject(request.getSubject())
                .description(request.getDescription())
                .originalFileName(originalFileName)
                .storedFileName(storedFileName)
                .fileType(contentType)
                .fileSize(file.getSize())
                .uploadedAt(Instant.now())
                .uploadedBy(user)
                .build();

        noteRepository.save(note);
        emailService.sendUploadConfirmation(user, note);
        return mapToNoteResponse(note);
    }

    /**
     * Maps a Note entity to a NoteResponse DTO and generates an S3 presigned URL.
     *
     * @param note The Note entity to map.
     * @return NoteResponse DTO.
     */
    public NoteResponse mapToNoteResponse(Note note) {
        String presignedUrl = fileStorageService.generatePresignedUrl(note.getStoredFileName());

        return NoteResponse.builder()
                .id(note.getId())
                .title(note.getTitle())
                .subject(note.getSubject())
                .description(note.getDescription())
                .originalFileName(note.getOriginalFileName())
                .storedFileName(note.getStoredFileName())
                .fileType(note.getFileType())
                .fileSize(note.getFileSize())
                .downloadUrl(presignedUrl)
                .uploadedAt(note.getUploadedAt())
                .uploadedBy(note.getUploadedBy().getUsername())
                .build();
    }

    /**
     * Retrieves a specific note by its ID.
     *
     * @param id The ID of the note.
     * @return NoteResponse containing note details.
     * @throws ResourceNotFoundException if the note is not found.
     */
    public NoteResponse getNoteById(Long id) {
        Note note = noteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Note not found with id: " + id));
        return mapToNoteResponse(note);
    }

    /**
     * Updates an existing note's metadata and optionally replaces its file.
     * Only the uploader or an admin can update the note.
     *
     * @param noteId The ID of the note to update.
     * @param request The update request containing new metadata or file.
     * @param username The username of the authenticated user requesting the update.
     * @return NoteResponse containing the updated note details.
     * @throws SecurityException if the user is not authorized to update the note.
     * @throws ResourceNotFoundException if the note is not found.
     */
    @Transactional
    @PreAuthorize("hasRole('ADMIN') or #username == principal.username")
    public NoteResponse updateNote(Long noteId, NoteUpdateRequest request, String username) {
        Note note = noteRepository.findById(noteId)
                .orElseThrow(() -> new ResourceNotFoundException("Note not found with id " + noteId));

        if (!note.getUploadedBy().getUsername().equals(username) &&
                SecurityContextHolder.getContext().getAuthentication().getAuthorities().stream()
                        .noneMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
            throw new SecurityException("You are not allowed to update this note");
        }

        // Update metadata
        if (request.getTitle() != null) note.setTitle(request.getTitle());
        if (request.getSubject() != null) note.setSubject(request.getSubject());
        if (request.getDescription() != null) note.setDescription(request.getDescription());

        // Update file if present
        MultipartFile file = request.getFile();
        if (file != null) {
            fileStorageService.deleteFile(note.getStoredFileName());
            String storedFileName = fileStorageService.storeFile(file);
            note.setOriginalFileName(file.getOriginalFilename());
            note.setStoredFileName(storedFileName);
            note.setFileType(file.getContentType());
            note.setFileSize(file.getSize());
        }

        Note updatedNote = noteRepository.save(note);
        return mapToNoteResponse(updatedNote);
    }

    /**
     * Deletes a note from the database and removes its file from S3.
     * Only the uploader or an admin can delete the note.
     *
     * @param id The ID of the note to delete.
     * @param username The username of the authenticated user requesting deletion.
     * @throws AccessDeniedException if the user is not authorized.
     * @throws ResourceNotFoundException if the note is not found.
     */
    @PreAuthorize("hasRole('ADMIN') or #username == principal.username")
    @Transactional
    public void deleteNote(Long id, String username) {
        Note note = noteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Note not found with id: " + id));

        if (!note.getUploadedBy().getUsername().equals(username) &&
                SecurityContextHolder.getContext().getAuthentication().getAuthorities()
                        .stream().noneMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"))) {
            throw new AccessDeniedException("You are not authorized to delete this note.");
        }

        fileStorageService.deleteFile(note.getStoredFileName());
        noteRepository.deleteById(id);
    }

    /**
     * Retrieves a paginated list of notes, optionally filtered by uploader, subject, and title.
     *
     * @param uploadedBy Optional filter by username.
     * @param subject Optional filter by subject.
     * @param title Optional filter by title.
     * @param pageable Pagination configuration.
     * @return Page of NoteResponse objects.
     */
    public Page<NoteResponse> listNotes(String uploadedBy, String subject, String title, Pageable pageable) {
        Specification<Note> spec = Specification.allOf();

        if (subject != null && !subject.isBlank()) {
            spec = spec.and((root, query, criteriaBuilder) ->
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("subject")), "%" + subject.toLowerCase() + "%"));
        }
        if (title != null && !title.isBlank()) {
            spec = spec.and((root, query, criteriaBuilder) ->
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("title")), "%" + title.toLowerCase() + "%"));
        }

        if (uploadedBy != null && !uploadedBy.isEmpty()) {
            spec = spec.and((root, query, criteriaBuilder) ->
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("uploadedBy").get("username")), "%" + uploadedBy.toLowerCase() + "%"));
        }

        return noteRepository.findAll(spec, pageable)
                .map(this::mapToNoteResponse);
    }

    /**
     * Generates a presigned URL to securely download a note.
     *
     * @param id The ID of the note.
     * @return A presigned S3 URL string.
     * @throws ResourceNotFoundException if the note is not found.
     */
    public String downloadNote(Long id) {
        Note note = noteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Note not found with id: " + id));

        return mapToNoteResponse(note).getDownloadUrl();
    }
}