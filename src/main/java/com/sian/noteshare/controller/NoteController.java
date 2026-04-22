package com.sian.noteshare.controller;

import com.sian.noteshare.dto.NoteResponse;
import com.sian.noteshare.dto.NoteUpdateRequest;
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

/**
 * REST Controller for managing Notes.
 * Provides endpoints for uploading, retrieving, searching, updating, and deleting notes,
 * as well as generating secure download links.
 */
@RestController
@RequestMapping("/notes")
@RequiredArgsConstructor
public class NoteController {

    private final NoteService noteService;

    /**
     * Endpoint to upload a new note document and metadata.
     * Requires the user to be authenticated.
     *
     * @param request Validated NoteUploadRequest containing multipart file and note details.
     * @return ResponseEntity containing the created NoteResponse.
     */
    @PostMapping("/upload")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<NoteResponse> uploadNote(@ModelAttribute @Valid NoteUploadRequest request) {
        NoteResponse response = noteService.uploadNote(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Retrieves a paginated list of all notes, with optional filtering.
     * Accessible to both public and authenticated users.
     *
     * @param uploadedBy Optional username to filter notes by a specific uploader.
     * @param subject Optional subject keyword to filter notes.
     * @param title Optional title keyword to filter notes.
     * @param pageable Pagination configuration (defaults to 9 items per page, sorted by newest).
     * @return ResponseEntity containing a Page of NoteResponse DTOs.
     */
    @GetMapping
    public ResponseEntity<Page<NoteResponse>> getAllNotes(
            @RequestParam(required = false) String uploadedBy,
            @RequestParam(required = false) String subject,
            @RequestParam(required = false) String title,
            @PageableDefault(size = 9, sort = "uploadedAt", direction = Sort.Direction.DESC) Pageable pageable) {

        return ResponseEntity.ok(noteService.listNotes(uploadedBy, subject, title, pageable));
    }

    /**
     * Retrieves the details of a specific note by its ID.
     *
     * @param id The ID of the note.
     * @return ResponseEntity containing the NoteResponse.
     */
    @GetMapping("/{id}")
    public ResponseEntity<NoteResponse> getNoteById(@PathVariable Long id) {
        return ResponseEntity.ok(noteService.getNoteById(id));
    }

    /**
     * Updates an existing note.
     * Requires the user to be authenticated and to be the owner of the note (or an ADMIN).
     *
     * @param noteId The ID of the note to update.
     * @param request The NoteUpdateRequest containing the fields or file to update.
     * @param authentication The Spring Security authentication object of the current user.
     * @return ResponseEntity containing the updated NoteResponse.
     */
    @PutMapping("/{id}")
    public ResponseEntity<NoteResponse> updateNote(
            @PathVariable("id") Long noteId,
            @ModelAttribute NoteUpdateRequest request,
            Authentication authentication) {

        String username = authentication.getName();
        NoteResponse updatedNote = noteService.updateNote(noteId, request, username);
        return ResponseEntity.ok(updatedNote);
    }

    /**
     * Deletes a specific note by ID.
     * Requires the user to be authenticated and to be the owner of the note (or an ADMIN).
     *
     * @param id The ID of the note to delete.
     * @param authentication The Spring Security authentication object of the current user.
     * @return ResponseEntity with no content upon successful deletion.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteNoteById(@PathVariable Long id, Authentication authentication) {
        noteService.deleteNote(id, authentication.getName());
        return ResponseEntity.noContent().build();
    }

    /**
     * Generates a presigned URL to download a note's file from S3.
     *
     * @param id The ID of the note.
     * @param redirect Boolean indicating whether to immediately redirect the client to the URL.
     * @return ResponseEntity containing the download URL, or a 302 Redirect to the S3 bucket.
     */
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