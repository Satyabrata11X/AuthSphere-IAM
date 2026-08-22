package com.authsphere.authsphere_backend.identity.token;

import com.authsphere.authsphere_backend.core.exception.InvalidVerificationTokenException;
import com.authsphere.authsphere_backend.core.exception.VerificationTokenExpiredException;
import com.authsphere.authsphere_backend.identity.email.EmailService;
import com.authsphere.authsphere_backend.identity.user.AccountStatus;
import com.authsphere.authsphere_backend.identity.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class VerificationServiceImpl implements VerificationService {

    private final VerificationTokenRepository verificationTokenRepository;
    private final EmailService emailService;

    @Value("${auth.verification-token-expiration}")
    private long verificationTokenExpiration;

    @Override
    @Transactional
    public void createVerificationToken(User user) {

        // Remove any existing verification token for this user
        verificationTokenRepository.deleteByUserId(user.getId());

        // Generate a new unique token
        String token = UUID.randomUUID().toString();

        // Calculate token expiry time
        LocalDateTime expiresAt = LocalDateTime.now()
                .plusSeconds(verificationTokenExpiration / 1000);

        // Create verification token
        VerificationToken verificationToken = VerificationToken.builder()
                .token(token)
                .user(user)
                .expiresAt(expiresAt)
                .build();

        // Save token
        verificationTokenRepository.save(verificationToken);

        // Send verification email
        emailService.sendVerificationEmail(user, token);
    }

    @Override
    @Transactional
    public void verifyToken(String token) {

        // Find token
        VerificationToken verificationToken =
                verificationTokenRepository.findByToken(token)
                        .orElseThrow(() ->
                                new InvalidVerificationTokenException(
                                        "Invalid verification token."
                                )
                        );

        // Check token expiry
        if (verificationToken.getExpiresAt()
                .isBefore(LocalDateTime.now())) {

            // Delete expired token
            verificationTokenRepository.delete(verificationToken);

            throw new VerificationTokenExpiredException(
                    "Verification token has expired."
            );
        }

        // Get associated user
        User user = verificationToken.getUser();

        // Mark email as verified
        user.setEmailVerified(true);

        // Activate account
        user.setAccountStatus(AccountStatus.ACTIVE);

        // Delete token after successful verification
        verificationTokenRepository.delete(verificationToken);
    }

    @Override
    @Transactional
    public void resendVerificationToken(User user) {

        // Generate a fresh token
        createVerificationToken(user);
    }
}