package com.sian.noteshare.exception;

/**
 * Exception thrown when an error occurs during file upload, download, or deletion in S3.
 */
public class FileStorageException extends RuntimeException{
    public FileStorageException(String message) { super(message); }
    public FileStorageException(String message, Throwable cause) { super(message, cause); }
}
