package com.sian.noteshare.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sian.noteshare.config.CustomUserDetailsService;
import com.sian.noteshare.config.JwtAuthFilter;
import com.sian.noteshare.config.OAuth2LoginSuccessHandler;
import com.sian.noteshare.dto.JwtAuthenticationResponse;
import com.sian.noteshare.dto.RegisterRequest;
import com.sian.noteshare.service.AuthService;
import com.sian.noteshare.util.JwtUtil;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Integration tests for the AuthController.
 * Verifies endpoint routing, JSON payload validation, and HTTP responses.
 */
@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false) // Disables Spring Security filters for this focused unit test
class AuthControllerIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    // Mock dependencies required by the controller and the security context
    @MockitoBean private AuthService authService;
    @MockitoBean private JwtUtil jwtUtil;
    @MockitoBean private CustomUserDetailsService customUserDetailsService;
    @MockitoBean private OAuth2LoginSuccessHandler oAuth2LoginSuccessHandler;
    @MockitoBean private JwtAuthFilter jwtAuthFilter;

    /**
     * Tests that a valid registration request returns an HTTP 200 OK and a JWT token.
     */
    @Test
    void register_ShouldReturn200AndToken_WhenPayloadIsValid() throws Exception {
        RegisterRequest request = new RegisterRequest("testuser", "password123", "test@example.com");
        JwtAuthenticationResponse mockResponse = new JwtAuthenticationResponse("mock-jwt-token");

        when(authService.register(any(RegisterRequest.class))).thenReturn(mockResponse);

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("mock-jwt-token"))
                .andExpect(jsonPath("$.tokenType").value("Bearer"));
    }

    /**
     * Tests that a registration payload with an invalid password length fails @Valid checks
     * and returns an HTTP 400 Bad Request.
     */
    @Test
    void register_ShouldReturn400_WhenPasswordTooShort() throws Exception {
        // Password is only 3 characters, violates @Size(min = 6)
        RegisterRequest request = new RegisterRequest("testuser", "123", "test@example.com");

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest()); // Handled by GlobalExceptionHandler
    }
}
