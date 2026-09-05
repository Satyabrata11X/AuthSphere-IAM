package com.authsphere.authsphere_backend.identity.mfa;

import com.authsphere.authsphere_backend.core.common.ApiResponse;
import com.authsphere.authsphere_backend.core.common.ApiStatus;
import com.authsphere.authsphere_backend.identity.auth.dto.response.LoginResponse;
import com.authsphere.authsphere_backend.identity.mfa.dto.request.MfaLoginVerifyRequest;
import com.authsphere.authsphere_backend.identity.mfa.dto.request.MfaVerifyRequest;
import com.authsphere.authsphere_backend.identity.mfa.dto.response.MfaSetupResponse;
import com.authsphere.authsphere_backend.identity.user.User;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth/mfa")
@RequiredArgsConstructor
public class MfaController {

    private final MfaService mfaService;

    /**
     * Starts MFA setup for the authenticated user.
     */
    @SecurityRequirement(name = "bearerAuth")
    @PostMapping("/setup")
    public ApiResponse<MfaSetupResponse> setupMfa(
            Authentication authentication) {

        User user = (User) authentication.getPrincipal();

        MfaSetupResponse response =
                mfaService.setupMfa(user);

        return ApiResponse.<MfaSetupResponse>builder()
                .success(true)
                .status(ApiStatus.SUCCESS)
                .message("MFA setup initialized successfully.")
                .data(response)
                .build();
    }


    /**
     * Enables MFA after verifying the user's TOTP code.
     */
    @SecurityRequirement(name = "bearerAuth")
    @PostMapping("/enable")
    public ApiResponse<Void> enableMfa(
            Authentication authentication,
            @Valid @RequestBody MfaVerifyRequest request) {

        User user = (User) authentication.getPrincipal();

        mfaService.enableMfa(
                user,
                request.getCode()
        );

        return ApiResponse.<Void>builder()
                .success(true)
                .status(ApiStatus.SUCCESS)
                .message("MFA enabled successfully.")
                .build();
    }


    /**
     * Verifies MFA during login and issues the real access token.
     */
    @PostMapping("/verify-login")
    public ApiResponse<LoginResponse> verifyLoginMfa(
            @Valid @RequestBody MfaLoginVerifyRequest request,
            HttpServletRequest httpRequest) {

        LoginResponse response =
                mfaService.verifyLoginMfa(
                        request,
                        httpRequest
                );

        return ApiResponse.<LoginResponse>builder()
                .success(true)
                .status(ApiStatus.SUCCESS)
                .message("MFA verification successful. Login completed.")
                .data(response)
                .build();
    }
}