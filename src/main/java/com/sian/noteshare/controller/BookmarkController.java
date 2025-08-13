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

@RestController
@RequestMapping("/bookmarks")
@RequiredArgsConstructor
public class BookmarkController {
    private final BookmarkService bookmarkService;

    @PostMapping("/{noteId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<BookmarkResponse> addBookmark(@PathVariable Long noteId,
                                                        @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(bookmarkService.addBookmark(noteId, userDetails.getUsername()));
    }

    @DeleteMapping("/{noteId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> removeBookmark(@PathVariable Long noteId,
                                               @AuthenticationPrincipal UserDetails userDetails) {
        bookmarkService.removeBookmark(noteId, userDetails.getUsername());
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<BookmarkResponse>> listBookmarks(
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(bookmarkService.listBookmarks(userDetails.getUsername()));
    }
}
