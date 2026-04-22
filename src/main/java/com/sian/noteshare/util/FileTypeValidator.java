package com.sian.noteshare.util;

import java.util.Arrays;
import java.util.List;

/**
 * Utility class to enforce restrictions on file types uploaded to the platform.
 */
public class FileTypeValidator {

    private static final List<String> ALLOWED_FILE_TYPES = Arrays.asList(
            "application/pdf",
            "text/plain",
            "image/png",
            "image/jpeg"
    );

    /**
     * Checks if a given MIME content type is permitted by the application.
     *
     * @param contentType The MIME type string of the file (e.g., "application/pdf").
     * @return true if the content type is allowed, false otherwise.
     */
    public static boolean isAllowed(String contentType) {
        return ALLOWED_FILE_TYPES.contains(contentType);
    }
}