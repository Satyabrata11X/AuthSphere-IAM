package com.authsphere.authsphere_backend.identity.superadmin.controller;

import com.authsphere.authsphere_backend.core.common.ApiResponse;
import com.authsphere.authsphere_backend.core.common.ApiStatus;
import com.authsphere.authsphere_backend.identity.superadmin.dto.SuperAdminUserResponse;
import com.authsphere.authsphere_backend.identity.superadmin.dto.SuperAdminUserStatusRequest;
import com.authsphere.authsphere_backend.identity.superadmin.service.SuperAdminUserService;
import com.authsphere.authsphere_backend.identity.user.User;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/superadmin/users")
@RequiredArgsConstructor
public class SuperAdminUserController {

    private final SuperAdminUserService superAdminUserService;


    // =========================================================
    // GET ALL USERS
    // =========================================================

    @SecurityRequirement(name = "bearerAuth")
    @GetMapping
    public ApiResponse<List<SuperAdminUserResponse>> getAllUsers() {

        List<SuperAdminUserResponse> users =
                superAdminUserService.getAllUsers();

        return ApiResponse.<List<SuperAdminUserResponse>>builder()
                .success(true)
                .status(ApiStatus.SUCCESS)
                .message("Super Admin user list retrieved successfully.")
                .data(users)
                .build();
    }


    // =========================================================
    // GET USER BY ID
    // =========================================================

    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/{userId}")
    public ApiResponse<SuperAdminUserResponse> getUserById(
            @PathVariable UUID userId
    ) {

        SuperAdminUserResponse user =
                superAdminUserService.getUserById(userId);

        return ApiResponse.<SuperAdminUserResponse>builder()
                .success(true)
                .status(ApiStatus.SUCCESS)
                .message(
                        "Super Admin user details retrieved successfully."
                )
                .data(user)
                .build();
    }


    // =========================================================
    // UPDATE USER ACCOUNT STATUS
    // =========================================================

    @SecurityRequirement(name = "bearerAuth")
    @PutMapping("/{userId}/status")
    public ApiResponse<SuperAdminUserResponse> updateUserStatus(
            @PathVariable UUID userId,
            @Valid @RequestBody SuperAdminUserStatusRequest request,
            Authentication authentication
    ) {

        User currentUser =
                (User) authentication.getPrincipal();

        SuperAdminUserResponse user =
                superAdminUserService.updateUserStatus(
                        userId,
                        request,
                        currentUser
                );

        return ApiResponse.<SuperAdminUserResponse>builder()
                .success(true)
                .status(ApiStatus.SUCCESS)
                .message(
                        "User account status updated successfully."
                )
                .data(user)
                .build();
    }
}