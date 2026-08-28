package com.authsphere.authsphere_backend.identity.auth.security;

import com.authsphere.authsphere_backend.identity.user.User;
import com.authsphere.authsphere_backend.identity.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AccountLockoutService {

    private static final int MAX_FAILED_ATTEMPTS = 5;

    private static final int FIRST_LOCK_MINUTES = 15;

    private static final int SUBSEQUENT_LOCK_HOURS = 1;

    private final UserRepository userRepository;


    public boolean isAccountLocked(User user) {

        LocalDateTime lockedUntil = user.getLockedUntil();

        // Account is not currently locked
        if (lockedUntil == null) {
            return false;
        }

        // Lock has expired
        if (!LocalDateTime.now().isBefore(lockedUntil)) {

            user.setLockedUntil(null);
            user.setFailedLoginAttempts(0);

            userRepository.saveAndFlush(user);

            return false;
        }

        // Account is still locked
        return true;
    }


    public void recordFailedLogin(User user) {

        int failedAttempts =
                user.getFailedLoginAttempts() + 1;

        user.setFailedLoginAttempts(failedAttempts);

        // Lock account after 5 failed attempts
        if (failedAttempts >= MAX_FAILED_ATTEMPTS) {

            int lockoutCount =
                    user.getLockoutCount() + 1;

            user.setLockoutCount(lockoutCount);

            LocalDateTime lockedUntil;

            // First lock → 15 minutes
            if (lockoutCount == 1) {

                lockedUntil =
                        LocalDateTime.now()
                                .plusMinutes(FIRST_LOCK_MINUTES);

            } else {

                // Second and subsequent locks → 1 hour
                lockedUntil =
                        LocalDateTime.now()
                                .plusHours(SUBSEQUENT_LOCK_HOURS);
            }

            user.setLockedUntil(lockedUntil);
        }

        userRepository.saveAndFlush(user);
    }


    public void recordSuccessfulLogin(User user) {

        // Reset failed login attempts
        user.setFailedLoginAttempts(0);

        // Remove any active lock
        user.setLockedUntil(null);

        // Update last successful login time
        user.setLastLoginAt(LocalDateTime.now());

        userRepository.saveAndFlush(user);
    }
}