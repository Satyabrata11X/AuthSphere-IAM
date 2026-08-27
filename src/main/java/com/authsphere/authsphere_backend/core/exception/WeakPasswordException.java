package com.authsphere.authsphere_backend.core.exception;

public class WeakPasswordException extends RuntimeException {

    public WeakPasswordException(String message) {
        super(message);
    }
}