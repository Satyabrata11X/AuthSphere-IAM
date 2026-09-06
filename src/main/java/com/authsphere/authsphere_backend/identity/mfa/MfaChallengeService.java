package com.authsphere.authsphere_backend.identity.mfa;

import com.authsphere.authsphere_backend.core.exception.InvalidMfaChallengeException;
import com.authsphere.authsphere_backend.identity.user.User;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class MfaChallengeService {

    private static final Duration CHALLENGE_EXPIRATION =
            Duration.ofMinutes(5);

    private final Map<String, MfaChallenge> challenges =
            new ConcurrentHashMap<>();

    public String createChallenge(User user) {

        String challengeToken =
                UUID.randomUUID().toString();

        challenges.put(
                challengeToken,
                new MfaChallenge(
                        user,
                        LocalDateTime.now()
                                .plus(CHALLENGE_EXPIRATION)
                )
        );

        return challengeToken;
    }

    public User getUserFromChallenge(
            String challengeToken) {

        MfaChallenge challenge =
                challenges.get(challengeToken);

        if (challenge == null) {
            throw new InvalidMfaChallengeException(
                    "Invalid or expired MFA challenge."
            );
        }

        if (LocalDateTime.now()
                .isAfter(challenge.expiresAt())) {

            challenges.remove(challengeToken);

            throw new InvalidMfaChallengeException(
                    "MFA challenge has expired."
            );
        }

        return challenge.user();
    }

    public void removeChallenge(
            String challengeToken) {

        challenges.remove(challengeToken);
    }

    private record MfaChallenge(
            User user,
            LocalDateTime expiresAt
    ) {
    }
}