package com.sian.noteshare.controller;

import com.sian.noteshare.config.CustomUserDetailsService;
import com.sian.noteshare.config.JwtAuthFilter;
import com.sian.noteshare.config.OAuth2LoginSuccessHandler;
import com.sian.noteshare.dto.NoteResponse;
import com.sian.noteshare.dto.NoteUploadRequest;
import com.sian.noteshare.service.NoteService;
import com.sian.noteshare.util.JwtUtil;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for NoteController endpoints.
 * Validates multipart file uploads and security authorizations.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class NoteControllerIntegrationTest {

    @Autowired private MockMvc mockMvc;

    @MockitoBean private NoteService noteService;
    @MockitoBean private JwtUtil jwtUtil;
    @MockitoBean private CustomUserDetailsService customUserDetailsService;
    @MockitoBean private OAuth2LoginSuccessHandler oAuth2LoginSuccessHandler;
    @MockitoBean private JavaMailSender mailSender;
    @MockitoBean private OAuth2AuthorizedClientService authorizedClientService;


    /**
     * Tests that an authenticated user can successfully upload a multipart file and note metadata.
     */
    @Test
    @WithMockUser(username = "testuser", roles = {"USER"}) // Simulates a logged-in user
    void uploadNote_ShouldReturn201_WhenAuthenticatedAndValid() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "test.pdf", MediaType.APPLICATION_PDF_VALUE, "dummy content".getBytes());
        NoteResponse mockResponse = NoteResponse.builder().id(1L).title("Test Title").build();

        when(noteService.uploadNote(any(NoteUploadRequest.class))).thenReturn(mockResponse);

        mockMvc.perform(multipart("/notes/upload")
                        .file(file)
                        .param("title", "Test Title")
                        .param("subject", "Math"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("Test Title"));
    }

    @Test
    void uploadNote_ShouldRedirectToOAuth2Login_WhenUnauthenticated() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.pdf",
                MediaType.APPLICATION_PDF_VALUE,
                "dummy content".getBytes()
        );

        mockMvc.perform(multipart("/notes/upload")
                        .file(file)
                        .param("title", "Test Title")
                        .with(csrf()))
                .andExpect(status().isFound())
                .andExpect(redirectedUrlPattern("**/oauth2/authorization/**"));
    }


}
