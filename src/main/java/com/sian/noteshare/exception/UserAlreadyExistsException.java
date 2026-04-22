package com.sian.noteshare.exception;

/**
 * Exception thrown during registration if the requested username or email is already taken.
 */
public class UserAlreadyExistsException extends RuntimeException{
    public UserAlreadyExistsException(String message) { super(message); }
}