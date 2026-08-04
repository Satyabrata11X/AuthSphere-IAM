package com.authsphere.authsphere_backend.identity.auth.service.impl;

import com.authsphere.authsphere_backend.core.common.ApiResponse;
import com.authsphere.authsphere_backend.core.common.ApiStatus;
import com.authsphere.authsphere_backend.core.exception.EmailAlreadyExistsException;
import com.authsphere.authsphere_backend.identity.auth.dto.request.RegisterRequest;
import com.authsphere.authsphere_backend.identity.auth.dto.response.RegisterResponse;
import com.authsphere.authsphere_backend.identity.auth.service.AuthenticationService;
import com.authsphere.authsphere_backend.identity.user.AccountStatus;
import com.authsphere.authsphere_backend.identity.user.User;
import com.authsphere.authsphere_backend.identity.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthenticationServiceImpl implements AuthenticationService {
    private final UserRepository userRepository;
    private final PasswordEncoder  passwordEncoder;


    @Override
    public ApiResponse<RegisterResponse> register(RegisterRequest request) {
        boolean emailExists = userRepository.existsByEmail(request.getEmail());
        String hashedPassword = passwordEncoder.encode(request.getPassword());

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
        userRepository.save(user);

        RegisterResponse response = RegisterResponse.builder()
                .userId(user.getId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .build();


        if (emailExists) {
            throw new EmailAlreadyExistsException(
                    "Email is already registered."
            );
        }

        return ApiResponse.<RegisterResponse>builder()
                .success(true)
                .status(ApiStatus.SUCCESS)
                .message("User registered successfully.")
                .data(response)
                .build();
    }
}
