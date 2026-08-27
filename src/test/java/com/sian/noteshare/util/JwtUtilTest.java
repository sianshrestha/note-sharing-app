package com.sian.noteshare.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the JwtUtil class.
 * Verifies token generation, claim extraction, and token validation logic.
 */
class JwtUtilTest {

    private JwtUtil jwtUtil;

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil();
        // Inject properties using Reflection since this is a unit test without Spring context
        // This is a dummy base64 encoded secret for testing purposes
        ReflectionTestUtils.setField(jwtUtil, "jwtSecret", "dGhpc2lzYXZlcnlsb25nYmFzZTY0ZW5jb2RlZHNlY3JldGtleWZvcmp3dHRlc3RpbmdwdXJwb3Nlcw==");
        ReflectionTestUtils.setField(jwtUtil, "jwtExpirationTime", 3600000L); // 1 hour
    }

    /**
     * Tests that generating a token returns a non-null, valid JWT string.
     */
    @Test
    void generateToken_ShouldReturnValidToken() {
        String token = jwtUtil.generateToken("test@example.com");

        assertNotNull(token);
        assertTrue(token.split("\\.").length == 3); // JWTs have 3 parts separated by dots
    }

    /**
     * Tests that parsing a valid token accurately extracts the subject (email).
     */
    @Test
    void getUsernameFromToken_ShouldReturnCorrectEmail() {
        String token = jwtUtil.generateToken("test@example.com");
        String extractedEmail = jwtUtil.getUsernameFromToken(token);

        assertEquals("test@example.com", extractedEmail);
    }

    /**
     * Tests that token validation returns true for a correctly signed, unexpired token.
     */
    @Test
    void validateToken_ShouldReturnTrue_WhenTokenIsValid() {
        String token = jwtUtil.generateToken("test@example.com");
        assertTrue(jwtUtil.validateToken(token));
    }

    /**
     * Tests that a token manufactured with a past expiration date fails validation.
     */
    @Test
    void validateToken_ShouldReturnFalse_WhenTokenIsExpired() {
        // Set expiration to 1 millisecond so it expires instantly
        ReflectionTestUtils.setField(jwtUtil, "jwtExpirationTime", 1L);
        String token = jwtUtil.generateToken("test@example.com");

        // Wait a fraction of a second to ensure it expires
        try { Thread.sleep(10); } catch (InterruptedException e) { }

        assertFalse(jwtUtil.validateToken(token));
    }
}
