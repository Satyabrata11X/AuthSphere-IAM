package com.authsphere.authsphere_backend.identity.auth.controller;


import com.authsphere.authsphere_backend.core.common.ApiStatus;
import com.authsphere.authsphere_backend.identity.auth.dto.request.ChangePasswordRequest;
import com.authsphere.authsphere_backend.identity.auth.dto.request.RegisterRequest;
import com.authsphere.authsphere_backend.identity.auth.dto.request.LoginRequest;
import com.authsphere.authsphere_backend.identity.auth.dto.request.ForgotPasswordRequest;
import com.authsphere.authsphere_backend.identity.auth.dto.request.ResetPasswordRequest;
import com.authsphere.authsphere_backend.identity.auth.dto.response.LoginResponse;
import com.authsphere.authsphere_backend.identity.auth.dto.response.RegisterResponse;
import com.authsphere.authsphere_backend.identity.auth.service.AuthenticationService;
import com.authsphere.authsphere_backend.core.common.ApiResponse;
import com.authsphere.authsphere_backend.identity.user.User;
import com.authsphere.authsphere_backend.identity.user.dto.UserProfileResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor

public class AuthenticationController {

    private final AuthenticationService authenticationService;

    @PostMapping("/register")
    public ApiResponse<RegisterResponse> register(
            @RequestBody RegisterRequest request) {

        return authenticationService.register(request);
    }

    @PostMapping("/login")
    public ApiResponse<LoginResponse> login(
            @RequestBody LoginRequest request) {

        return authenticationService.login(request);
    }

    @PostMapping("/forgot-password")
    public ApiResponse<Void> forgotPassword(
            @RequestBody ForgotPasswordRequest request) {

        return authenticationService.forgotPassword(
                request.getEmail()
        );
    }

    @PostMapping("/reset-password")
    public ApiResponse<Void> resetPassword(
            @RequestBody ResetPasswordRequest request) {

        return authenticationService.resetPassword(
                request.getToken(),
                request.getNewPassword()
        );
    }

    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/me")
    public ApiResponse<UserProfileResponse> getCurrentUser(
            Authentication authentication) {

        User user = (User) authentication.getPrincipal();

        UserProfileResponse profile =
                UserProfileResponse.builder()
                        .id(user.getId())
                        .firstName(user.getFirstName())
                        .lastName(user.getLastName())
                        .email(user.getEmail())
                        .employeeId(user.getEmployeeId())
                        .phoneNumber(user.getPhoneNumber())
                        .profileImageUrl(user.getProfileImageUrl())
                        .emailVerified(user.isEmailVerified())
                        .mfaEnabled(user.isMfaEnabled())
                        .accountStatus(user.getAccountStatus())
                        .lastLoginAt(user.getLastLoginAt())
                        .build();

        return ApiResponse.<UserProfileResponse>builder()
                .success(true)
                .status(ApiStatus.SUCCESS)
                .message("Authenticated user.")
                .data(profile)
                .build();
    }

    @SecurityRequirement(name = "bearerAuth")
    @PostMapping("/change-password")
    public ApiResponse<Void> changePassword(
            Authentication authentication,
            @RequestBody ChangePasswordRequest request) {

        User user = (User) authentication.getPrincipal();

        return authenticationService.changePassword(
                user.getEmail(),
                request
        );
    }

    @GetMapping("/reset-password")
    public ApiResponse<Void> validateResetPasswordToken(
            @RequestParam String token) {

        return authenticationService.validateResetToken(token);
    }

}
