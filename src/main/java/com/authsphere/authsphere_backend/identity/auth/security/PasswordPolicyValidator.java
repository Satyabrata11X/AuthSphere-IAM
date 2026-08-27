package com.authsphere.authsphere_backend.identity.auth.security;

import org.springframework.stereotype.Component;

@Component
public class PasswordPolicyValidator {

    public boolean isValid(String password) {

        if (password == null || password.length() < 8) {
            return false;
        }

        boolean hasUppercase =
                password.matches(".*[A-Z].*");

        boolean hasLowercase =
                password.matches(".*[a-z].*");

        boolean hasNumber =
                password.matches(".*[0-9].*");

        boolean hasSpecialCharacter =
                password.matches(".*[^a-zA-Z0-9].*");

        return hasUppercase
                && hasLowercase
                && hasNumber
                && hasSpecialCharacter;
    }
}