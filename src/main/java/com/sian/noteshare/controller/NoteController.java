package com.sian.noteshare.controller;

import com.sian.noteshare.dto.NoteResponse;
import com.sian.noteshare.dto.NoteUploadRequest;
import com.sian.noteshare.service.NoteService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.Collections;

@RestController
@RequestMapping("/notes")
@RequiredArgsConstructor
public class NoteController {

    private final NoteService noteService;

    @PostMapping("/upload")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<NoteResponse> uploadNote(@ModelAttribute @Valid NoteUploadRequest request) {
        NoteResponse response = noteService.uploadNote(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<Page<NoteResponse>> getAllNotes(
            @RequestParam(required = false) String uploadedBy,
            @RequestParam(required = false) String subject,
            @RequestParam(required = false) String title,
            @PageableDefault(size = 10, sort = "uploadedAt", direction = Sort.Direction.DESC) Pageable pageable) {

        return ResponseEntity.ok(noteService.listNotes(uploadedBy, subject, title, pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<NoteResponse> getNoteById(@PathVariable Long id) {
        return ResponseEntity.ok(noteService.getNoteById(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteNoteById(@PathVariable Long id, Authentication authentication) {
        noteService.deleteNote(id, authentication.getName());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/download")
    public ResponseEntity<?> downloadNote(
            @PathVariable Long id, @RequestParam(defaultValue = "false") boolean redirect) {
        String downloadUrl = noteService.downloadNote(id);

        if (redirect) {
            return ResponseEntity.status(HttpStatus.FOUND)
                    .location(URI.create(downloadUrl))
                    .build();
        }
        return ResponseEntity.ok(Collections.singletonMap("downloadUrl", downloadUrl));
    }
}
