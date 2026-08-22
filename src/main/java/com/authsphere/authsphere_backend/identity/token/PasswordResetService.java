package com.authsphere.authsphere_backend.identity.token;

import com.authsphere.authsphere_backend.identity.user.User;

public interface PasswordResetService {

    void createResetToken(User user);

    void resetPassword(String token, String newPassword);

    void validateResetToken(String token);
}