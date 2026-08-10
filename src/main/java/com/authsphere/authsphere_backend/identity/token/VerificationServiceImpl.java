package com.authsphere.authsphere_backend.identity.token;

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

        verificationTokenRepository.deleteByUserId(user.getId());

        String token = UUID.randomUUID().toString();

        LocalDateTime expiresAt =
                LocalDateTime.now()
                        .plusSeconds(verificationTokenExpiration / 1000);

        VerificationToken verificationToken =
                VerificationToken.builder()
                        .token(token)
                        .user(user)
                        .expiresAt(expiresAt)
                        .build();

        verificationTokenRepository.save(verificationToken);

        // Send verification email
        emailService.sendVerificationEmail(user, token);
    }

    @Override
    @Transactional
    public void verifyToken(String token) {

        VerificationToken verificationToken =
                verificationTokenRepository.findByToken(token)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Invalid verification token."
                                ));

        // Check whether token has expired
        if (verificationToken.getExpiresAt().isBefore(LocalDateTime.now())) {

            verificationTokenRepository.delete(verificationToken);

            throw new IllegalArgumentException(
                    "Verification token has expired."
            );
        }

        User user = verificationToken.getUser();

        // Mark email as verified
        user.setEmailVerified(true);

        // Activate the account
        user.setAccountStatus(AccountStatus.ACTIVE);

        // Remove the token after successful verification
        verificationTokenRepository.delete(verificationToken);
    }

    @Override
    @Transactional
    public void resendVerificationToken(User user) {

        // Create a fresh verification token
        createVerificationToken(user);
    }
}