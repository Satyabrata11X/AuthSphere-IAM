package com.authsphere.authsphere_backend.identity.auth.security;

import com.authsphere.authsphere_backend.identity.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class LoginAttemptService {

    private final LoginAttemptRepository loginAttemptRepository;

    public void recordAttempt(
            User user,
            boolean success,
            String ipAddress
    ) {

        LoginAttempt loginAttempt = LoginAttempt.builder()
                .user(user)
                .success(success)
                .ipAddress(ipAddress)
                .attemptedAt(LocalDateTime.now())
                .build();

        loginAttemptRepository.save(loginAttempt);
    }
}