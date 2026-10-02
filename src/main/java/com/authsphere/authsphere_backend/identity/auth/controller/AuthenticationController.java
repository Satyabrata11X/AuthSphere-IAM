package com.authsphere.authsphere_backend.identity.auth.controller;

import com.authsphere.authsphere_backend.core.common.ApiResponse;
import com.authsphere.authsphere_backend.core.common.ApiStatus;
import com.authsphere.authsphere_backend.identity.auth.dto.request.ChangePasswordRequest;
import com.authsphere.authsphere_backend.identity.auth.dto.request.ForgotPasswordRequest;
import com.authsphere.authsphere_backend.identity.auth.dto.request.LoginRequest;
import com.authsphere.authsphere_backend.identity.auth.dto.request.RegisterRequest;
import com.authsphere.authsphere_backend.identity.auth.dto.request.ResetPasswordRequest;
import com.authsphere.authsphere_backend.identity.auth.dto.response.LoginResponse;
import com.authsphere.authsphere_backend.identity.auth.dto.response.RegisterResponse;
import com.authsphere.authsphere_backend.identity.auth.service.AuthenticationService;
import com.authsphere.authsphere_backend.identity.permission.Permission;
import com.authsphere.authsphere_backend.identity.role.Role;
import com.authsphere.authsphere_backend.identity.user.User;
import com.authsphere.authsphere_backend.identity.auth.dto.response.UserProfileResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
            @RequestBody LoginRequest request,
            HttpServletRequest httpRequest) {

        return authenticationService.login(
                request,
                httpRequest
        );
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

        /*
         * Extract roles from the authenticated user.
         */
        List<String> roles = user.getRoles()
                .stream()
                .map(Role::getName)
                .toList();

        /*
         * Extract all permissions granted through the user's roles.
         *
         * distinct() prevents duplicate permissions if multiple
         * roles grant the same permission.
         */
        List<String> permissions = user.getRoles()
                .stream()
                .flatMap(role -> role.getPermissions().stream())
                .map(Permission::getName)
                .distinct()
                .toList();

        /*
         * Build authenticated user profile.
         */
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
                        .roles(roles)
                        .permissions(permissions)
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

    @SecurityRequirement(name = "bearerAuth")
    @PostMapping("/logout")
    public ApiResponse<Void> logout(
            Authentication authentication,
            HttpServletRequest httpRequest) {

        User user = (User) authentication.getPrincipal();

        String authHeader =
                httpRequest.getHeader("Authorization");

        String accessToken =
                authHeader.substring(7);

        return authenticationService.logout(
                user,
                accessToken
        );
    }

    @SecurityRequirement(name = "bearerAuth")
    @PostMapping("/logout-all")
    public ApiResponse<Void> logoutAll(
            Authentication authentication) {

        User user = (User) authentication.getPrincipal();

        return authenticationService.logoutAll(user);
    }
}