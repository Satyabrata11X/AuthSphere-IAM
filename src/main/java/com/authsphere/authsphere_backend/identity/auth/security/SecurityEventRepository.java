package com.authsphere.authsphere_backend.identity.auth.security;

import com.authsphere.authsphere_backend.identity.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface SecurityEventRepository
        extends JpaRepository<SecurityEvent, UUID> {

    /*
     * Events for a specific user.
     */
    List<SecurityEvent> findByUserOrderByOccurredAtDesc(
            User user
    );

    /*
     * Recent security events across the platform.
     */
    List<SecurityEvent> findTop50ByOrderByOccurredAtDesc();

    /*
     * Security events belonging to a company.
     */
    @Query("""
        SELECT e
        FROM SecurityEvent e
        JOIN e.user u
        WHERE u.company.id = :companyId
        ORDER BY e.occurredAt DESC
    """)
    List<SecurityEvent> findCompanySecurityEvents(
            UUID companyId
    );

    /*
     * Count security events for a company.
     */
    @Query("""
        SELECT COUNT(e)
        FROM SecurityEvent e
        JOIN e.user u
        WHERE u.company.id = :companyId
    """)
    long countCompanySecurityEvents(
            UUID companyId
    );
}