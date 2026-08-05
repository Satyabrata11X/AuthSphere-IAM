package com.authsphere.authsphere_backend.core.exception;

public class InvalidCredentialsException extends RuntimeException {

    public InvalidCredentialsException(String message) {
        super(message);
    }

}