package com.sian.noteshare.controller;

import com.sian.noteshare.dto.UserProfileResponse;
import com.sian.noteshare.dto.UserProfileUpdateRequest;
import com.sian.noteshare.service.UserProfileService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users/profile")
@RequiredArgsConstructor
public class UserProfileController {

    private final UserProfileService userProfileService;

    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<UserProfileResponse> getMyProfile(Authentication authentication) {
        String username = authentication.getName();
        UserProfileResponse userProfile = userProfileService.getUserProfile(username);
        return ResponseEntity.ok(userProfile);
    }

    @GetMapping("/{username}")
    public ResponseEntity<UserProfileResponse> getUserProfile(@PathVariable String username) {
        UserProfileResponse userProfile = userProfileService.getUserProfile(username);
        return ResponseEntity.ok(userProfile);
    }

    @PutMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> updateMyProfile(Authentication authentication,
            @Valid @RequestBody UserProfileUpdateRequest request) {
        userProfileService.updateUserProfile(authentication, request);
        return ResponseEntity.ok().body("Profile updated successfully. Please log in again to see changes.");
    }
}
