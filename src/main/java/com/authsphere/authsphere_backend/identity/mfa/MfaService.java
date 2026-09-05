package com.authsphere.authsphere_backend.identity.mfa;

import com.authsphere.authsphere_backend.core.exception.InvalidCredentialsException;
import com.authsphere.authsphere_backend.identity.auth.dto.response.LoginResponse;
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

        // Prevent setup if MFA is already enabled
        if (existingConfiguration != null
                && existingConfiguration.isEnabled()) {

            throw new IllegalStateException(
                    "MFA is already enabled for this account."
            );
        }

        // Generate a new TOTP secret
        String secret =
                totpService.generateSecret();

        MfaConfiguration configuration;

        if (existingConfiguration == null) {

            configuration =
                    MfaConfiguration.builder()
                            .user(user)
                            .secret(secret)
                            .enabled(false)
                            .build();

        } else {

            existingConfiguration.setSecret(secret);
            existingConfiguration.setEnabled(false);

            configuration = existingConfiguration;
        }

        // Save MFA configuration
        mfaConfigurationRepository.save(configuration);

        // Generate standard otpauth URI
        QrData qrData =
                new QrData.Builder()
                        .label(user.getEmail())
                        .secret(secret)
                        .issuer(ISSUER)
                        .build();

        String otpauthUri =
                qrData.getUri();

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
    public void enableMfa(
            User user,
            String code) {

        MfaConfiguration configuration =
                mfaConfigurationRepository
                        .findByUser(user)
                        .orElseThrow(() ->
                                new IllegalStateException(
                                        "MFA setup has not been initialized."
                                )
                        );

        // Prevent enabling an already enabled configuration
        if (configuration.isEnabled()) {

            throw new IllegalStateException(
                    "MFA is already enabled for this account."
            );
        }

        // Verify the TOTP code
        boolean validCode =
                totpService.verifyCode(
                        configuration.getSecret(),
                        code
                );

        if (!validCode) {

            throw new IllegalArgumentException(
                    "Invalid MFA verification code."
            );
        }

        // Enable MFA configuration
        configuration.setEnabled(true);

        mfaConfigurationRepository.save(configuration);

        // Synchronize MFA status on User
        user.setMfaEnabled(true);

        // Explicitly persist User
        userRepository.save(user);
    }


    /**
     * Verifies MFA during login and issues the real JWT.
     *
     * The user must provide:
     * 1. A valid temporary MFA challenge token
     * 2. A valid 6-digit TOTP code
     */
    public LoginResponse verifyLoginMfa(
            MfaLoginVerifyRequest request,
            HttpServletRequest httpRequest) {

        // 1. Get the temporary MFA challenge token
        String challengeToken =
                request.getChallengeToken();

        // 2. Resolve the user from the challenge
        User user =
                mfaChallengeService.getUserFromChallenge(
                        challengeToken
                );

        // 3. Find MFA configuration
        MfaConfiguration configuration =
                mfaConfigurationRepository
                        .findByUser(user)
                        .orElseThrow(() ->
                                new InvalidCredentialsException(
                                        "MFA configuration not found."
                                )
                        );

        // 4. Make sure MFA is actually enabled
        if (!configuration.isEnabled()
                || !user.isMfaEnabled()) {

            throw new InvalidCredentialsException(
                    "MFA is not enabled for this account."
            );
        }

        // 5. Verify the 6-digit TOTP code
        boolean validCode =
                totpService.verifyCode(
                        configuration.getSecret(),
                        request.getCode()
                );

        if (!validCode) {

            throw new InvalidCredentialsException(
                    "Invalid MFA verification code."
            );
        }

        // 6. Get request information
        String ipAddress =
                httpRequest.getRemoteAddr();

        String userAgent =
                httpRequest.getHeader("User-Agent");

        // 7. Calculate session expiration
        LocalDateTime sessionExpiresAt =
                LocalDateTime.now()
                        .plus(Duration.ofMillis(jwtExpiration));

        // 8. Create authenticated session
        Session session =
                sessionService.createSession(
                        user,
                        ipAddress,
                        userAgent,
                        sessionExpiresAt
                );

        // 9. Generate the real JWT
        String accessToken =
                jwtService.generateToken(
                        user,
                        session.getId()
                );

        // 10. Invalidate the temporary challenge
        mfaChallengeService.removeChallenge(
                challengeToken
        );

        // 11. Return the real access token
        return LoginResponse.builder()
                .accessToken(accessToken)
                .build();
    }
}