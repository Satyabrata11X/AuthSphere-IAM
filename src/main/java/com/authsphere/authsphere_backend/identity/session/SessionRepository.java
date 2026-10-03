package com.authsphere.authsphere_backend.identity.session;

import com.authsphere.authsphere_backend.identity.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SessionRepository extends JpaRepository<Session, UUID> {

    List<Session> findByUserAndStatus(
            User user,
            SessionStatus status
    );

    Optional<Session> findByIdAndUser(
            UUID id,
            User user
    );

    List<Session> findByUserAndStatusNot(
            User user,
            SessionStatus status
    );

    long countByStatus(SessionStatus status);

    /*
     * All active sessions ordered by latest activity.
     */
    List<Session> findByStatusOrderByLastActivityAtDesc(
            SessionStatus status
    );

    /*
     * Sessions belonging to users of a particular company.
     */
    @Query("""
        SELECT s
        FROM Session s
        JOIN s.user u
        WHERE u.company.id = :companyId
          AND s.status = :status
        ORDER BY s.lastActivityAt DESC
    """)
    List<Session> findCompanySessions(
            UUID companyId,
            SessionStatus status
    );

    /*
     * Count active sessions belonging to a company.
     */
    @Query("""
        SELECT COUNT(s)
        FROM Session s
        JOIN s.user u
        WHERE u.company.id = :companyId
          AND s.status = :status
    """)
    long countCompanySessions(
            UUID companyId,
            SessionStatus status
    );
}