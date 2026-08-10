package com.authsphere.authsphere_backend.identity.auth.service.impl;

import com.authsphere.authsphere_backend.core.common.ApiResponse;
import com.authsphere.authsphere_backend.core.common.ApiStatus;
import com.authsphere.authsphere_backend.core.exception.EmailAlreadyExistsException;
import com.authsphere.authsphere_backend.core.exception.InvalidCredentialsException;
import com.authsphere.authsphere_backend.identity.auth.dto.request.LoginRequest;
import com.authsphere.authsphere_backend.identity.auth.dto.request.RegisterRequest;
import com.authsphere.authsphere_backend.identity.auth.dto.response.LoginResponse;
import com.authsphere.authsphere_backend.identity.auth.dto.response.RegisterResponse;
import com.authsphere.authsphere_backend.identity.auth.service.AuthenticationService;
import com.authsphere.authsphere_backend.identity.auth.service.JwtService;
import com.authsphere.authsphere_backend.identity.token.VerificationService;
import com.authsphere.authsphere_backend.identity.user.AccountStatus;
import com.authsphere.authsphere_backend.identity.user.User;
import com.authsphere.authsphere_backend.identity.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthenticationServiceImpl implements AuthenticationService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final VerificationService verificationService;


    @Override
    public ApiResponse<RegisterResponse> register(RegisterRequest request) {

        // 1. Check if email already exists
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new EmailAlreadyExistsException(
                    "Email is already registered."
            );
        }

        // 2. Hash password
        String hashedPassword =
                passwordEncoder.encode(request.getPassword());

        // 3. Create user
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

        // 4. Save user
        userRepository.save(user);

        // 5. Create verification token
        // This also sends the verification email
        verificationService.createVerificationToken(user);

        // 6. Prepare response
        RegisterResponse response = RegisterResponse.builder()
                .userId(user.getId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .build();

        // 7. Return response
        return ApiResponse.<RegisterResponse>builder()
                .success(true)
                .status(ApiStatus.SUCCESS)
                .message(
                        "User registered successfully. " +
                                "Please verify your email."
                )
                .data(response)
                .build();
    }


    @Override
    public ApiResponse<LoginResponse> login(LoginRequest request) {

        // 1. Find user by email
        Optional<User> optionalUser =
                userRepository.findByEmail(request.getEmail());

        // 2. User not found
        if (optionalUser.isEmpty()) {
            throw new InvalidCredentialsException(
                    "Invalid email or password."
            );
        }

        User user = optionalUser.get();

        // 3. Check password
        boolean passwordMatches =
                passwordEncoder.matches(
                        request.getPassword(),
                        user.getPasswordHash()
                );

        if (!passwordMatches) {
            throw new InvalidCredentialsException(
                    "Invalid email or password."
            );
        }

        // 4. Check email verification
        if (!user.isEmailVerified()) {
            throw new InvalidCredentialsException(
                    "Please verify your email before logging in."
            );
        }

        // 5. Check account status
        if (user.getAccountStatus() != AccountStatus.ACTIVE) {
            throw new InvalidCredentialsException(
                    "Your account is not active."
            );
        }

        // 6. Generate JWT
        String accessToken =
                jwtService.generateToken(user);

        // 7. Prepare login response
        LoginResponse response = LoginResponse.builder()
                .accessToken(accessToken)
                .build();

        // 8. Return response
        return ApiResponse.<LoginResponse>builder()
                .success(true)
                .status(ApiStatus.SUCCESS)
                .message("Login successful.")
                .data(response)
                .build();
    }
}