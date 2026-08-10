package com.authsphere.authsphere_backend.identity.token;

import com.authsphere.authsphere_backend.identity.user.User;

public interface VerificationService {

    void createVerificationToken(User user);

    void verifyToken(String token);

    void resendVerificationToken(User user);
}