package com.sian.noteshare.exception;

/**
 * Exception thrown when a user attempts to log in with incorrect credentials.
 */
public class InvalidCredentialsException extends RuntimeException{
    public InvalidCredentialsException(String message) { super(message); }
}