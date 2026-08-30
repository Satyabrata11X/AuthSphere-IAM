package com.authsphere.authsphere_backend.identity.auth.service.impl;

import com.authsphere.authsphere_backend.core.common.ApiResponse;
import com.authsphere.authsphere_backend.core.common.ApiStatus;
import java.time.Duration;
import java.time.LocalDateTime;
import com.authsphere.authsphere_backend.core.exception.EmailAlreadyExistsException;
import com.authsphere.authsphere_backend.core.exception.InvalidCredentialsException;
import com.authsphere.authsphere_backend.core.exception.PasswordSameAsOldException;
import com.authsphere.authsphere_backend.core.exception.WeakPasswordException;
import com.authsphere.authsphere_backend.identity.auth.dto.request.ChangePasswordRequest;
import com.authsphere.authsphere_backend.identity.auth.dto.request.LoginRequest;
import com.authsphere.authsphere_backend.identity.auth.dto.request.RegisterRequest;
import com.authsphere.authsphere_backend.identity.auth.dto.response.LoginResponse;
import com.authsphere.authsphere_backend.identity.auth.dto.response.RegisterResponse;
import com.authsphere.authsphere_backend.identity.auth.security.AccountLockoutService;
import com.authsphere.authsphere_backend.identity.auth.security.LoginAttemptService;
import com.authsphere.authsphere_backend.identity.auth.security.PasswordPolicyValidator;
import com.authsphere.authsphere_backend.identity.auth.security.SecurityEventService;
import com.authsphere.authsphere_backend.identity.auth.security.SecurityEventType;
import com.authsphere.authsphere_backend.identity.auth.service.AuthenticationService;
import com.authsphere.authsphere_backend.identity.auth.service.JwtService;
import com.authsphere.authsphere_backend.identity.session.Session;
import com.authsphere.authsphere_backend.identity.session.SessionService;
import com.authsphere.authsphere_backend.identity.token.PasswordResetService;
import com.authsphere.authsphere_backend.identity.token.VerificationService;
import com.authsphere.authsphere_backend.identity.user.AccountStatus;
import com.authsphere.authsphere_backend.identity.user.User;
import com.authsphere.authsphere_backend.identity.user.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthenticationServiceImpl implements AuthenticationService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final VerificationService verificationService;
    private final PasswordResetService passwordResetService;
    private final PasswordPolicyValidator passwordPolicyValidator;
    private final LoginAttemptService loginAttemptService;
    private final AccountLockoutService accountLockoutService;
    private final SecurityEventService securityEventService;
    private final SessionService sessionService;

    @Value("${jwt.expiration}")
    private long jwtExpiration;


    @Override
    public ApiResponse<RegisterResponse> register(RegisterRequest request) {

        // 1. Check if email already exists
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new EmailAlreadyExistsException(
                    "Email is already registered."
            );
        }

        // 2. Validate password strength
        if (!passwordPolicyValidator.isValid(request.getPassword())) {
            throw new WeakPasswordException(
                    "Password must contain at least 8 characters, "
                            + "one uppercase letter, one lowercase letter, "
                            + "one number, and one special character."
            );
        }

        // 3. Hash password
        String hashedPassword =
                passwordEncoder.encode(request.getPassword());

        // 4. Create user
        User user = User.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(request.getEmail())
                .passwordHash(hashedPassword)
                .employeeId(UUID.randomUUID().toString())
                .emailVerified(false)
                .mfaEnabled(false)
                .accountStatus(AccountStatus.PENDING_VERIFICATION)
                .build();

        // 5. Save user
        userRepository.save(user);

        // 6. Create verification token
        // This also sends the verification email
        verificationService.createVerificationToken(user);

        // 7. Prepare response
        RegisterResponse response = RegisterResponse.builder()
                .userId(user.getId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .build();

        // 8. Return response
        return ApiResponse.<RegisterResponse>builder()
                .success(true)
                .status(ApiStatus.SUCCESS)
                .message(
                        "User registered successfully. "
                                + "Please verify your email."
                )
                .data(response)
                .build();
    }


    @Override
    public ApiResponse<LoginResponse> login(
            LoginRequest request,
            HttpServletRequest httpRequest) {

        // 1. Get IP address of the login request
        String ipAddress = httpRequest.getRemoteAddr();

        // 2. Find user by email
        Optional<User> optionalUser =
                userRepository.findByEmail(request.getEmail());

        // 3. User not found
        if (optionalUser.isEmpty()) {
            throw new InvalidCredentialsException(
                    "Invalid email or password."
            );
        }

        User user = optionalUser.get();

        // 4. Check whether the account is currently locked
        if (accountLockoutService.isAccountLocked(user)) {
            throw new InvalidCredentialsException(
                    "Account is temporarily locked. Please try again later."
            );
        }

        // 5. Check password
        boolean passwordMatches =
                passwordEncoder.matches(
                        request.getPassword(),
                        user.getPasswordHash()
                );

        // 6. Wrong password
        if (!passwordMatches) {

            // Record login attempt history
            loginAttemptService.recordAttempt(
                    user,
                    false,
                    ipAddress
            );

            // Update account lockout state
            accountLockoutService.recordFailedLogin(user);

            // Record security event
            securityEventService.recordEvent(
                    user,
                    SecurityEventType.LOGIN_FAILED,
                    false,
                    ipAddress,
                    "Invalid password"
            );

            throw new InvalidCredentialsException(
                    "Invalid email or password."
            );
        }

        // 7. Check email verification
        if (!user.isEmailVerified()) {
            throw new InvalidCredentialsException(
                    "Please verify your email before logging in."
            );
        }

        // 8. Check account status
        if (user.getAccountStatus() != AccountStatus.ACTIVE) {
            throw new InvalidCredentialsException(
                    "Your account is not active."
            );
        }

        // 9. Record successful login attempt
        loginAttemptService.recordAttempt(
                user,
                true,
                ipAddress
        );

        // 10. Reset failed attempts
        accountLockoutService.recordSuccessfulLogin(user);

        // 11. Record security event
        securityEventService.recordEvent(
                user,
                SecurityEventType.LOGIN_SUCCESS,
                true,
                ipAddress,
                "User logged in successfully"
        );

        // 12. Create authentication session
        LocalDateTime sessionExpiresAt =
                LocalDateTime.now().plus(Duration.ofMillis(jwtExpiration));

        Session session = sessionService.createSession(
                user,
                ipAddress,
                httpRequest.getHeader("User-Agent"),
                sessionExpiresAt
        );

// 13. Generate JWT with session ID
        String accessToken =
                jwtService.generateToken(
                        user,
                        session.getId()
                );

// 14. Prepare login response
        LoginResponse response =
                LoginResponse.builder()
                        .accessToken(accessToken)
                        .build();

        // 15. Return response
        return ApiResponse.<LoginResponse>builder()
                .success(true)
                .status(ApiStatus.SUCCESS)
                .message("Login successful.")
                .data(response)
                .build();
    }


    @Override
    public ApiResponse<Void> changePassword(
            String email,
            ChangePasswordRequest request) {

        // 1. Find authenticated user
        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new InvalidCredentialsException(
                                "Authenticated user not found."
                        )
                );

        // 2. Verify current password
        boolean passwordMatches =
                passwordEncoder.matches(
                        request.getCurrentPassword(),
                        user.getPasswordHash()
                );

        if (!passwordMatches) {
            throw new InvalidCredentialsException(
                    "Current password is incorrect."
            );
        }

        // 3. Prevent using the same password
        if (passwordEncoder.matches(
                request.getNewPassword(),
                user.getPasswordHash())) {

            throw new PasswordSameAsOldException(
                    "New password must be different from current password."
            );
        }

        // 4. Hash the new password
        String newPasswordHash =
                passwordEncoder.encode(request.getNewPassword());

        // 5. Update password
        user.setPasswordHash(newPasswordHash);

        // 6. Save user
        userRepository.save(user);

        // 7. Return success
        return ApiResponse.<Void>builder()
                .success(true)
                .status(ApiStatus.SUCCESS)
                .message("Password changed successfully.")
                .build();
    }


    @Override
    public ApiResponse<Void> forgotPassword(String email) {

        userRepository.findByEmail(email)
                .ifPresent(passwordResetService::createResetToken);

        return ApiResponse.<Void>builder()
                .success(true)
                .status(ApiStatus.SUCCESS)
                .message(
                        "If an account exists for this email, "
                                + "a password reset link has been sent."
                )
                .build();
    }


    @Override
    public ApiResponse<Void> resetPassword(
            String token,
            String newPassword) {

        passwordResetService.resetPassword(
                token,
                newPassword
        );

        return ApiResponse.<Void>builder()
                .success(true)
                .status(ApiStatus.SUCCESS)
                .message("Password reset successfully.")
                .build();
    }


    @Override
    public ApiResponse<Void> validateResetToken(String token) {

        passwordResetService.validateResetToken(token);

        return ApiResponse.<Void>builder()
                .success(true)
                .status(ApiStatus.SUCCESS)
                .message("Password reset token is valid.")
                .build();
    }

    @Override
    public ApiResponse<Void> logout(
            User user,
            String accessToken) {

        // Extract session ID from JWT
        UUID sessionId =
                jwtService.extractSessionId(accessToken);

        // Revoke the current session
        sessionService.revokeSession(
                sessionId,
                user
        );

        return ApiResponse.<Void>builder()
                .success(true)
                .status(ApiStatus.SUCCESS)
                .message("Logout successful.")
                .build();
    }

    @Override
    public ApiResponse<Void> logoutAll(User user) {

        // Revoke all active sessions for the user
        sessionService.revokeAllSessions(user);

        return ApiResponse.<Void>builder()
                .success(true)
                .status(ApiStatus.SUCCESS)
                .message("Logged out from all sessions successfully.")
                .build();
    }
}