package com.authsphere.authsphere_backend.identity.session;

import com.authsphere.authsphere_backend.identity.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

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
}