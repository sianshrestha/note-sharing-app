package com.sian.noteshare.controller;

import com.sian.noteshare.dto.JwtAuthenticationResponse;
import com.sian.noteshare.dto.LoginRequest;
import com.sian.noteshare.dto.RegisterRequest;
import com.sian.noteshare.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST Controller responsible for handling user authentication and registration.
 * Exposes publicly accessible endpoints for user onboarding and session creation.
 */
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /**
     * Endpoint to authenticate an existing user.
     *
     * @param request Validated LoginRequest containing email and password.
     * @return ResponseEntity containing the JwtAuthenticationResponse (the token).
     */
    @PostMapping("/login")
    public ResponseEntity<JwtAuthenticationResponse> login( @Valid @RequestBody LoginRequest request) {
        JwtAuthenticationResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }

    /**
     * Endpoint to register a new user in the system.
     *
     * @param request Validated RegisterRequest containing username, email, and password.
     * @return ResponseEntity containing the JwtAuthenticationResponse (the token) to log them in immediately.
     */
    @PostMapping("/register")
    public ResponseEntity<JwtAuthenticationResponse> register(@Valid @RequestBody RegisterRequest request) {
        JwtAuthenticationResponse response = authService.register(request);
        return ResponseEntity.ok(response);
    }
}