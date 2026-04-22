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

/**
 * REST Controller for accessing and updating user profile data.
 */
@RestController
@RequestMapping("/users/profile")
@RequiredArgsConstructor
public class UserProfileController {

    private final UserProfileService userProfileService;

    /**
     * Retrieves the profile information of the currently authenticated user.
     *
     * @param authentication The Spring Security authentication object.
     * @return ResponseEntity containing the UserProfileResponse.
     */
    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<UserProfileResponse> getMyProfile(Authentication authentication) {
        String username = authentication.getName();
        UserProfileResponse userProfile = userProfileService.getUserProfile(username);
        return ResponseEntity.ok(userProfile);
    }

    /**
     * Retrieves the public profile information of any user by their username.
     *
     * @param username The username of the requested profile.
     * @return ResponseEntity containing the UserProfileResponse.
     */
    @GetMapping("/{username}")
    public ResponseEntity<UserProfileResponse> getUserProfile(@PathVariable String username) {
        UserProfileResponse userProfile = userProfileService.getUserProfile(username);
        return ResponseEntity.ok(userProfile);
    }

    /**
     * Updates the currently authenticated user's profile data (e.g., username, email).
     *
     * @param authentication The Spring Security authentication object.
     * @param request Validated UserProfileUpdateRequest containing new profile data.
     * @return ResponseEntity with a success message.
     */
    @PutMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> updateMyProfile(Authentication authentication,
                                             @Valid @RequestBody UserProfileUpdateRequest request) {
        userProfileService.updateUserProfile(authentication, request);
        return ResponseEntity.ok().body("Profile updated successfully. Please log in again to see changes.");
    }
}