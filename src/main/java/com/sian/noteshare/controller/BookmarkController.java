package com.sian.noteshare.controller;

import com.sian.noteshare.dto.BookmarkResponse;
import com.sian.noteshare.service.BookmarkService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller for managing user bookmarks.
 * All endpoints in this controller require the user to be authenticated.
 */
@RestController
@RequestMapping("/bookmarks")
@RequiredArgsConstructor
public class BookmarkController {

    private final BookmarkService bookmarkService;

    /**
     * Adds a note to the authenticated user's bookmarks.
     *
     * @param noteId The ID of the note to bookmark.
     * @param userDetails The authenticated user's details.
     * @return ResponseEntity containing the created BookmarkResponse.
     */
    @PostMapping("/{noteId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<BookmarkResponse> addBookmark(@PathVariable Long noteId,
                                                        @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(bookmarkService.addBookmark(noteId, userDetails.getUsername()));
    }

    /**
     * Removes a note from the authenticated user's bookmarks.
     *
     * @param noteId The ID of the note to un-bookmark.
     * @param userDetails The authenticated user's details.
     * @return ResponseEntity with no content upon successful deletion.
     */
    @DeleteMapping("/{noteId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> removeBookmark(@PathVariable Long noteId,
                                               @AuthenticationPrincipal UserDetails userDetails) {
        bookmarkService.removeBookmark(noteId, userDetails.getUsername());
        return ResponseEntity.noContent().build();
    }

    /**
     * Retrieves all bookmarks for the currently authenticated user.
     *
     * @param userDetails The authenticated user's details.
     * @return ResponseEntity containing a list of BookmarkResponse DTOs.
     */
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<BookmarkResponse>> listBookmarks(
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(bookmarkService.listBookmarks(userDetails.getUsername()));
    }
}