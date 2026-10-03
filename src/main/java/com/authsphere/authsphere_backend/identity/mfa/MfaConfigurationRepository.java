package com.authsphere.authsphere_backend.identity.mfa;

import com.authsphere.authsphere_backend.identity.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface MfaConfigurationRepository
        extends JpaRepository<MfaConfiguration, UUID> {

    Optional<MfaConfiguration> findByUser(User user);

    Optional<MfaConfiguration> findByUserId(UUID userId);

    boolean existsByUser(User user);

    long countByEnabledTrue();
}