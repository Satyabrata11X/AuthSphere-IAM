package com.authsphere.authsphere_backend.identity.auth.security;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface LoginAttemptRepository
        extends JpaRepository<LoginAttempt, UUID> {
}