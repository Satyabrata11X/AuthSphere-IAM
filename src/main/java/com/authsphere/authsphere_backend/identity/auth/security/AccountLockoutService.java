
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
    private final SecurityEventService securityEventService;

    public boolean isAccountLocked(User user) {

        LocalDateTime lockedUntil = user.getLockedUntil();

        if (lockedUntil == null) {
            return false;
        }

        if (!LocalDateTime.now().isBefore(lockedUntil)) {

            user.setLockedUntil(null);
            user.setFailedLoginAttempts(0);

            userRepository.saveAndFlush(user);

            return false;
        }

        return true;
    }

    public void recordFailedLogin(User user, String ipAddress) {

        if (isAccountLocked(user)) {
            return;
        }

        int failedAttempts = user.getFailedLoginAttempts() + 1;

        user.setFailedLoginAttempts(failedAttempts);

        if (failedAttempts >= MAX_FAILED_ATTEMPTS) {

            int lockoutCount = user.getLockoutCount() + 1;

            user.setLockoutCount(lockoutCount);

            LocalDateTime lockedUntil;

            if (lockoutCount == 1) {
                lockedUntil = LocalDateTime.now()
                        .plusMinutes(FIRST_LOCK_MINUTES);
            } else {
                lockedUntil = LocalDateTime.now()
                        .plusHours(SUBSEQUENT_LOCK_HOURS);
            }

            user.setLockedUntil(lockedUntil);

            userRepository.saveAndFlush(user);

            securityEventService.recordEvent(
                    user,
                    SecurityEventType.ACCOUNT_LOCKED,
                    false,
                    ipAddress,
                    "Account locked after "
                            + MAX_FAILED_ATTEMPTS
                            + " consecutive failed authentication attempts."
            );

            return;
        }

        userRepository.saveAndFlush(user);
    }

    public void recordSuccessfulLogin(User user) {

        user.setFailedLoginAttempts(0);
        user.setLockedUntil(null);
        user.setLastLoginAt(LocalDateTime.now());

        userRepository.saveAndFlush(user);
    }
}
