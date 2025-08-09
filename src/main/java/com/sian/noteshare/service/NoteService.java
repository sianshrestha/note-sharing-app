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
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Paths;
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

        String filePath = fileStorageService.storeFile(file);
        String originalFileName = file.getOriginalFilename();
        String storedFileName = Paths.get(filePath).getFileName().toString();
        String downloadUrl = "/notes/download/" + storedFileName;

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
                .filePath(filePath)
                .fileSize(file.getSize())
                .downloadUrl(downloadUrl)
                .uploadedAt(LocalDateTime.now())
                .uploadedBy(user)
                .build();

        Note savedNote = noteRepository.save(note);

        return NoteResponse.builder()
                .id(savedNote.getId())
                .title(savedNote.getTitle())
                .subject(savedNote.getSubject())
                .description(savedNote.getDescription())
                .originalFileName(savedNote.getOriginalFileName())
                .storedFileName(savedNote.getStoredFileName())
                .fileType(savedNote.getFileType())
                .fileSize(savedNote.getFileSize())
                .filePath(savedNote.getFilePath())
                .downloadUrl(savedNote.getDownloadUrl())
                .uploadedAt(savedNote.getUploadedAt())
                .uploadedBy(savedNote.getUploadedBy().getUsername())
                .build();
    }
}
