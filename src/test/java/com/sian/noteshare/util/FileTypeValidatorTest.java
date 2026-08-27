package com.sian.noteshare.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for the FileTypeValidator utility class.
 * Ensures only permitted file formats can be uploaded to the platform.
 */
class FileTypeValidatorTest {

    /**
     * Tests that explicitly permitted MIME types return true.
     */
    @Test
    void isAllowed_ShouldReturnTrue_WhenTypeIsAllowed() {
        assertTrue(FileTypeValidator.isAllowed("application/pdf"));
        assertTrue(FileTypeValidator.isAllowed("text/plain"));
        assertTrue(FileTypeValidator.isAllowed("image/png"));
        assertTrue(FileTypeValidator.isAllowed("image/jpeg"));
    }

    /**
     * Tests that blocked or unknown MIME types return false.
     */
    @Test
    void isAllowed_ShouldReturnFalse_WhenTypeIsBlocked() {
        assertFalse(FileTypeValidator.isAllowed("application/x-msdownload")); // .exe
        assertFalse(FileTypeValidator.isAllowed("application/zip"));
        assertFalse(FileTypeValidator.isAllowed("text/html"));
        assertFalse(FileTypeValidator.isAllowed(""));
        assertFalse(FileTypeValidator.isAllowed(null));
    }
}
