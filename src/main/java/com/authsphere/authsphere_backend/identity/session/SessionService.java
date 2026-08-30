package com.authsphere.authsphere_backend.identity.session;

import com.authsphere.authsphere_backend.identity.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SessionService {

    private final SessionRepository sessionRepository;

    public Session createSession(
            User user,
            String ipAddress,
            String userAgent,
            LocalDateTime expiresAt
    ) {

        Session session = Session.builder()
                .user(user)
                .ipAddress(ipAddress)
                .userAgent(userAgent)
                .lastActivityAt(LocalDateTime.now())
                .expiresAt(expiresAt)
                .status(SessionStatus.ACTIVE)
                .build();

        return sessionRepository.save(session);
    }


    public List<Session> getActiveSessions(User user) {

        return sessionRepository.findByUserAndStatus(
                user,
                SessionStatus.ACTIVE
        );
    }


    // Find a session using the session ID stored in the JWT
    public Session getSession(UUID sessionId) {

        return sessionRepository.findById(sessionId)
                .orElse(null);
    }


    public void revokeSession(
            UUID sessionId,
            User user
    ) {

        Session session =
                sessionRepository
                        .findByIdAndUser(sessionId, user)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Session not found."
                                )
                        );

        if (session.getStatus() != SessionStatus.ACTIVE) {
            return;
        }

        session.setStatus(SessionStatus.REVOKED);
        session.setRevokedAt(LocalDateTime.now());

        sessionRepository.save(session);
    }


    public void revokeAllSessions(User user) {

        List<Session> sessions =
                sessionRepository.findByUserAndStatus(
                        user,
                        SessionStatus.ACTIVE
                );

        LocalDateTime now = LocalDateTime.now();

        for (Session session : sessions) {

            session.setStatus(SessionStatus.REVOKED);
            session.setRevokedAt(now);
        }

        sessionRepository.saveAll(sessions);
    }


    public boolean isSessionActive(Session session) {

        if (session.getStatus() != SessionStatus.ACTIVE) {
            return false;
        }

        if (LocalDateTime.now().isAfter(session.getExpiresAt())) {

            session.setStatus(SessionStatus.EXPIRED);

            sessionRepository.save(session);

            return false;
        }

        return true;
    }


    public void updateLastActivity(Session session) {

        session.setLastActivityAt(LocalDateTime.now());

        sessionRepository.save(session);
    }
}