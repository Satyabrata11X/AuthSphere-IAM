package com.authsphere.authsphere_backend.identity.auth.security;

import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.UUID;

public interface LoginAttemptRepository
        extends JpaRepository<LoginAttempt, UUID> {

    @Query("""
    SELECT
        FUNCTION('DATE', l.attemptedAt),
        l.success,
        COUNT(l)
    FROM LoginAttempt l
    WHERE l.attemptedAt >= :startDate
      AND l.attemptedAt < :endDate
    GROUP BY FUNCTION('DATE', l.attemptedAt), l.success
    ORDER BY FUNCTION('DATE', l.attemptedAt)
""")
    List<Object[]> findDashboardAuthenticationActivity(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );
}