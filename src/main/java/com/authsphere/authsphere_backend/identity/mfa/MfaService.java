
package com.authsphere.authsphere_backend.identity.mfa;

import com.authsphere.authsphere_backend.core.exception.InvalidCredentialsException;
import com.authsphere.authsphere_backend.identity.auth.dto.response.LoginResponse;
import com.authsphere.authsphere_backend.identity.auth.security.AccountLockoutService;
import com.authsphere.authsphere_backend.identity.auth.security.LoginAttemptService;
import com.authsphere.authsphere_backend.identity.auth.security.SecurityEventService;
import com.authsphere.authsphere_backend.identity.auth.security.SecurityEventType;
import com.authsphere.authsphere_backend.identity.auth.service.JwtService;
import com.authsphere.authsphere_backend.identity.mfa.dto.request.MfaLoginVerifyRequest;
import com.authsphere.authsphere_backend.identity.mfa.dto.response.MfaSetupResponse;
import com.authsphere.authsphere_backend.identity.session.Session;
import com.authsphere.authsphere_backend.identity.session.SessionService;
import com.authsphere.authsphere_backend.identity.user.User;
import com.authsphere.authsphere_backend.identity.user.UserRepository;

import dev.samstevens.totp.qr.QrData;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class MfaService {

    private final MfaConfigurationRepository mfaConfigurationRepository;
    private final UserRepository userRepository;
    private final TotpService totpService;
    private final MfaChallengeService mfaChallengeService;
    private final JwtService jwtService;
    private final SessionService sessionService;
    private final LoginAttemptService loginAttemptService;
    private final SecurityEventService securityEventService;
    private final AccountLockoutService accountLockoutService;

    private static final String ISSUER = "AuthSphere";

    @Value("${jwt.expiration}")
    private long jwtExpiration;

    /**
     * Starts MFA setup for the authenticated user.
     */
    public MfaSetupResponse setupMfa(User user) {

        MfaConfiguration existingConfiguration =
                mfaConfigurationRepository
                        .findByUser(user)
                        .orElse(null);

        if (existingConfiguration != null
                && existingConfiguration.isEnabled()) {

            throw new IllegalStateException(
                    "MFA is already enabled for this account."
            );
        }

        String secret = totpService.generateSecret();

        MfaConfiguration configuration;

        if (existingConfiguration == null) {

            configuration = MfaConfiguration.builder()
                    .user(user)
                    .secret(secret)
                    .enabled(false)
                    .build();

        } else {

            existingConfiguration.setSecret(secret);
            existingConfiguration.setEnabled(false);

            configuration = existingConfiguration;
        }

        mfaConfigurationRepository.save(configuration);

        QrData qrData = new QrData.Builder()
                .label(user.getEmail())
                .secret(secret)
                .issuer(ISSUER)
                .build();

        String otpauthUri = qrData.getUri();

        return MfaSetupResponse.builder()
                .secret(secret)
                .issuer(ISSUER)
                .accountName(user.getEmail())
                .otpauthUri(otpauthUri)
                .build();
    }

    /**
     * Enables MFA after verifying the user's TOTP code.
     */
    public void enableMfa(User user, String code) {

        MfaConfiguration configuration =
                mfaConfigurationRepository
                        .findByUser(user)
                        .orElseThrow(() ->
                                new IllegalStateException(
                                        "MFA setup has not been initialized."
                                )
                        );

        if (configuration.isEnabled()) {

            throw new IllegalStateException(
                    "MFA is already enabled for this account."
            );
        }

        boolean validCode = totpService.verifyCode(
                configuration.getSecret(),
                code
        );

        if (!validCode) {

            throw new IllegalArgumentException(
                    "Invalid MFA verification code."
            );
        }

        configuration.setEnabled(true);

        mfaConfigurationRepository.save(configuration);

        user.setMfaEnabled(true);

        userRepository.save(user);
    }

    /**
     * Verifies MFA during login and issues the real JWT.
     */
    public LoginResponse verifyLoginMfa(
            MfaLoginVerifyRequest request,
            HttpServletRequest httpRequest) {

        // 1. Get temporary MFA challenge token
        String challengeToken = request.getChallengeToken();

        // 2. Resolve user from challenge
        User user = mfaChallengeService.getUserFromChallenge(
                challengeToken
        );

        // 3. Check account lockout
        if (accountLockoutService.isAccountLocked(user)) {

            mfaChallengeService.removeChallenge(challengeToken);

            throw new InvalidCredentialsException(
                    "Account is temporarily locked. Please try again later."
            );
        }

        // 4. Find MFA configuration
        MfaConfiguration configuration =
                mfaConfigurationRepository
                        .findByUser(user)
                        .orElseThrow(() ->
                                new InvalidCredentialsException(
                                        "MFA configuration not found."
                                )
                        );

        // 5. Verify MFA is enabled
        if (!configuration.isEnabled()
                || !user.isMfaEnabled()) {

            throw new InvalidCredentialsException(
                    "MFA is not enabled for this account."
            );
        }

        // 6. Get request information
        String ipAddress = httpRequest.getRemoteAddr();

        String userAgent = httpRequest.getHeader("User-Agent");

        // 7. Verify TOTP code
        boolean validCode = totpService.verifyCode(
                configuration.getSecret(),
                request.getCode()
        );

        // 8. Handle failed MFA verification
        if (!validCode) {

            loginAttemptService.recordAttempt(
                    user,
                    false,
                    ipAddress
            );

            accountLockoutService.recordFailedLogin(user, ipAddress);

            securityEventService.recordEvent(
                    user,
                    SecurityEventType.LOGIN_FAILED,
                    false,
                    ipAddress,
                    "Invalid MFA verification code"
            );

            // Check whether this failure triggered lockout
            if (accountLockoutService.isAccountLocked(user)) {

                mfaChallengeService.removeChallenge(challengeToken);

                throw new InvalidCredentialsException(
                        "Account is temporarily locked. Please try again later."
                );
            }

            throw new InvalidCredentialsException(
                    "Invalid MFA verification code."
            );
        }

        // 9. Calculate session expiration
        LocalDateTime sessionExpiresAt =
                LocalDateTime.now()
                        .plus(Duration.ofMillis(jwtExpiration));

        // 10. Create authenticated session
        Session session = sessionService.createSession(
                user,
                ipAddress,
                userAgent,
                sessionExpiresAt
        );

        // 11. Generate real JWT
        String accessToken = jwtService.generateToken(
                user,
                session.getId()
        );

        // 12. Record successful MFA login attempt
        loginAttemptService.recordAttempt(
                user,
                true,
                ipAddress
        );

        // 13. Record successful security event
        securityEventService.recordEvent(
                user,
                SecurityEventType.LOGIN_SUCCESS,
                true,
                ipAddress,
                "User logged in successfully using MFA"
        );

        // 14. Reset lockout after complete authentication
        accountLockoutService.recordSuccessfulLogin(user);

        // 15. Invalidate temporary MFA challenge
        mfaChallengeService.removeChallenge(challengeToken);

        // 16. Return access token
        return LoginResponse.builder()
                .accessToken(accessToken)
                .build();
    }
}
