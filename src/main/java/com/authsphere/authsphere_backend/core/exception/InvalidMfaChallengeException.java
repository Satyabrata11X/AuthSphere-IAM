package com.authsphere.authsphere_backend.core.exception;

public class InvalidMfaChallengeException extends RuntimeException {

    public InvalidMfaChallengeException(String message) {
        super(message);
    }
}