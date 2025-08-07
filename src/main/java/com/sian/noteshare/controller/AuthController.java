package com.sian.noteshare.controller;

import com.sian.noteshare.dto.JwtAuthenticationResponse;
import com.sian.noteshare.dto.LoginRequest;
import com.sian.noteshare.dto.RegisterRequest;
import com.sian.noteshare.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<JwtAuthenticationResponse> login(@RequestBody LoginRequest request) {
        JwtAuthenticationResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/register")
    public ResponseEntity<JwtAuthenticationResponse> register(@RequestBody RegisterRequest request) {
        JwtAuthenticationResponse response = authService.register(request);
        return ResponseEntity.ok(response);
    }


}
