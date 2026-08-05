package com.authsphere.authsphere_backend.identity.auth.service;

import com.authsphere.authsphere_backend.identity.user.User;

public interface JwtService {

    String generateToken(User user);

    String extractUsername(String token);

    boolean isTokenValid(String token , User user);
}
