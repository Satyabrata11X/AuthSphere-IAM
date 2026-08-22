package com.authsphere.authsphere_backend.core.exception;

public class PasswordSameAsOldException extends RuntimeException {

    public PasswordSameAsOldException(String message) {
        super(message);
    }
}