package com.sian.noteshare.exception;

/**
 * Exception thrown when a requested entity (User, Note, Bookmark) is not found in the database.
 */
public class ResourceNotFoundException extends RuntimeException{
    public ResourceNotFoundException(String message) { super(message); }
    public ResourceNotFoundException(String message, Throwable cause) { super(message, cause); }
}
