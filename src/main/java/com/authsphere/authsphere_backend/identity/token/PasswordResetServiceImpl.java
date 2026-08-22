package com.authsphere.authsphere_backend.identity.token;

import com.authsphere.authsphere_backend.core.exception.InvalidPasswordResetTokenException;
import com.authsphere.authsphere_backend.core.exception.PasswordResetTokenExpiredException;
import com.authsphere.authsphere_backend.identity.email.EmailService;
import com.authsphere.authsphere_backend.identity.user.User;
import com.authsphere.authsphere_backend.identity.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PasswordResetServiceImpl implements PasswordResetService {

    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    @Value("${auth.password-reset-token-expiration}")
    private long passwordResetTokenExpiration;

    @Override
    @Transactional
    public void createResetToken(User user) {

        // Delete any previous reset token
        passwordResetTokenRepository.deleteByUserId(user.getId());

        // Generate secure random token
        String token = UUID.randomUUID().toString();

        // Token expires after configured duration
        LocalDateTime expiresAt =
                LocalDateTime.now()
                        .plusSeconds(passwordResetTokenExpiration / 1000);

        PasswordResetToken resetToken =
                PasswordResetToken.builder()
                        .token(token)
                        .user(user)
                        .expiresAt(expiresAt)
                        .build();

        passwordResetTokenRepository.save(resetToken);

        // Send password reset email
        emailService.sendPasswordResetEmail(user, token);
    }

    @Override
    public void validateResetToken(String token) {

        PasswordResetToken resetToken =
                passwordResetTokenRepository.findByToken(token)
                        .orElseThrow(() ->
                                new InvalidPasswordResetTokenException(
                                        "Invalid password reset token."
                                )
                        );

        if (resetToken.getExpiresAt()
                .isBefore(LocalDateTime.now())) {

            passwordResetTokenRepository.delete(resetToken);

            throw new PasswordResetTokenExpiredException(
                    "Password reset token has expired."
            );
        }
    }

    @Override
    @Transactional
    public void resetPassword(
            String token,
            String newPassword) {

        // Find reset token
        PasswordResetToken resetToken =
                passwordResetTokenRepository.findByToken(token)
                        .orElseThrow(() ->
                                new InvalidPasswordResetTokenException(
                                      "Invalid password reset token."
                                )
                        );

        // Check token expiration
        if (resetToken.getExpiresAt()
                .isBefore(LocalDateTime.now())) {

            passwordResetTokenRepository.delete(resetToken);

            throw new PasswordResetTokenExpiredException(
                    "Password reset token has expired."
            );
        }

        // Get associated user
        User user = resetToken.getUser();

        // Hash the new password
        String newPasswordHash =
                passwordEncoder.encode(newPassword);

        // Update password
        user.setPasswordHash(newPasswordHash);

        userRepository.save(user);

        // Delete token so it cannot be reused
        passwordResetTokenRepository.delete(resetToken);
    }
}