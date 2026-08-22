package com.authsphere.authsphere_backend.core.exception;

public class PasswordResetTokenExpiredException extends RuntimeException {

    public PasswordResetTokenExpiredException(String message) {
        super(message);
    }
}