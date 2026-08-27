package com.sian.noteshare.service;

import com.sian.noteshare.dto.JwtAuthenticationResponse;
import com.sian.noteshare.dto.LoginRequest;
import com.sian.noteshare.dto.RegisterRequest;
import com.sian.noteshare.entity.User;
import com.sian.noteshare.exception.InvalidCredentialsException;
import com.sian.noteshare.exception.UserAlreadyExistsException;
import com.sian.noteshare.repository.UserRepository;
import com.sian.noteshare.util.JwtUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for the AuthService.
 * Verifies registration constraints, password encoding, and authentication flows.
 */
@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private AuthenticationManager authenticationManager;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private JwtUtil jwtUtil;
    @Mock private EmailService emailService;

    @InjectMocks private AuthService authService;

    /**
     * Tests that a valid registration successfully encodes the password, saves the user, and emails them.
     */
    @Test
    void register_ShouldSaveUserAndReturnToken_WhenSuccess() {
        RegisterRequest request = new RegisterRequest("newuser", "password123", "new@example.com");

        when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.empty());
        when(userRepository.findByUsername(request.getUsername())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(any())).thenReturn("hashed-password");
        when(jwtUtil.generateToken(any())).thenReturn("mock-jwt-token");

        JwtAuthenticationResponse response = authService.register(request);

        assertEquals("mock-jwt-token", response.getAccessToken());
        verify(userRepository, times(1)).save(any(User.class));
        verify(emailService, times(1)).sendWelcomeEmail(any(User.class));
    }

    /**
     * Tests that registering with an existing email throws a UserAlreadyExistsException.
     */
    @Test
    void register_ShouldThrowException_WhenEmailExists() {
        RegisterRequest request = new RegisterRequest("newuser", "password", "exist@example.com");
        when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.of(new User()));

        assertThrows(UserAlreadyExistsException.class, () -> authService.register(request));
        verify(userRepository, never()).save(any());
    }

    /**
     * Tests that providing invalid credentials during login throws an InvalidCredentialsException.
     */
    @Test
    void login_ShouldThrowException_WhenAuthFails() {
        LoginRequest request = new LoginRequest("test@example.com", "wrongpassword");

        when(authenticationManager.authenticate(any()))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        assertThrows(InvalidCredentialsException.class, () -> authService.login(request));
    }
}