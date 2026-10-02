package com.authsphere.authsphere_backend.identity.user;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {

    boolean existsByEmail(String email);

    Optional<User> findByEmail(String email);

    long countByMfaEnabledTrue();

    @Query("""
        SELECT COUNT(DISTINCT u.id)
        FROM User u
        JOIN u.roles r
        WHERE r.name IN ('ADMIN', 'SUPER_ADMIN')
    """)
    long countAdministrators();
}