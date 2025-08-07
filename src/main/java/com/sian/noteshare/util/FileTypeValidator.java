package com.sian.noteshare.util;

import java.util.Arrays;
import java.util.List;

public class FileTypeValidator {
    private static final List<String> ALLOWED_FILE_TYPES = Arrays.asList(
            "application/pdf",
            "text/plain",
            "image/png",
            "image/jpeg"
    );

    public static boolean isAllowed(String contentType) {
        return ALLOWED_FILE_TYPES.contains(contentType);
    }
}

