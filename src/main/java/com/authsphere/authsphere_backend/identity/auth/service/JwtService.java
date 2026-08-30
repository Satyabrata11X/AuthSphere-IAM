package com.authsphere.authsphere_backend.identity.auth.service;

import com.authsphere.authsphere_backend.identity.user.User;

import java.util.UUID;

public interface JwtService {

    String generateToken(User user, UUID sessionId);

    String extractUsername(String token);

    UUID extractSessionId(String token);

    boolean isTokenValid(String token, User user);
}