package com.authsphere.authsphere_backend.identity.email;

import com.authsphere.authsphere_backend.identity.user.User;

public interface EmailService {

    void sendVerificationEmail(User user, String token);

    void sendPasswordResetEmail(User user, String token);

}