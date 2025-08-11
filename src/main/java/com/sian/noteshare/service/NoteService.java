package com.sian.noteshare.service;

import com.sian.noteshare.dto.NoteResponse;
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

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class NoteService {

    private final NoteRepository noteRepository;
    private final UserRepository userRepository;
    private final FileStorageService fileStorageService;

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
                .uploadedAt(LocalDateTime.now())
                .uploadedBy(user)
                .build();

        noteRepository.save(note);
        return mapToNoteResponse(note);
    }

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

    public NoteResponse getNoteById(Long id) {
        Note note = noteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Note not found with id: " + id));
        return mapToNoteResponse(note);
    }

    @PreAuthorize("hasRole('ADMIN') or #username == principal.username")
    @Transactional
    public void deleteNote(Long id, String username) {
        Note note = noteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Note not found with id: " + id));

        if (!note.getUploadedBy().getUsername().equals(username) &&
                !SecurityContextHolder.getContext().getAuthentication().getAuthorities()
                        .stream().anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"))) {
            throw new AccessDeniedException("You are not authorized to delete this note.");
        }

        fileStorageService.deleteFile(note.getStoredFileName());
        noteRepository.deleteById(id);
    }

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
}
