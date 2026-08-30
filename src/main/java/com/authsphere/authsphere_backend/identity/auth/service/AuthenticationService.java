package com.authsphere.authsphere_backend.identity.auth.service;

import com.authsphere.authsphere_backend.core.common.ApiResponse;
import com.authsphere.authsphere_backend.identity.auth.dto.request.ChangePasswordRequest;
import com.authsphere.authsphere_backend.identity.auth.dto.request.LoginRequest;
import com.authsphere.authsphere_backend.identity.auth.dto.request.RegisterRequest;
import com.authsphere.authsphere_backend.identity.auth.dto.response.LoginResponse;
import com.authsphere.authsphere_backend.identity.auth.dto.response.RegisterResponse;
import com.authsphere.authsphere_backend.identity.user.User;
import jakarta.servlet.http.HttpServletRequest;

public interface AuthenticationService {

    ApiResponse<RegisterResponse> register(RegisterRequest request);

    ApiResponse<LoginResponse> login(
            LoginRequest request,
            HttpServletRequest httpRequest
    );

    ApiResponse<Void> changePassword(
            String email,
            ChangePasswordRequest request
    );

    ApiResponse<Void> forgotPassword(String email);

    ApiResponse<Void> resetPassword(
            String token,
            String newPassword
    );

    ApiResponse<Void> validateResetToken(String token);

    ApiResponse<Void> logout(
            User user,
            String accessToken
    );

    ApiResponse<Void> logoutAll(User user);
}