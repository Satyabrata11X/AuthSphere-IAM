package com.authsphere.authsphere_backend.identity.superadmin.controller;

import com.authsphere.authsphere_backend.core.common.ApiResponse;
import com.authsphere.authsphere_backend.core.common.ApiStatus;
import com.authsphere.authsphere_backend.identity.superadmin.dto.SuperAdminDashboardResponse;
import com.authsphere.authsphere_backend.identity.superadmin.service.SuperAdminDashboardService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/superadmin")
@RequiredArgsConstructor
public class SuperAdminDashboardController {

    private final SuperAdminDashboardService superAdminDashboardService;

    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/dashboard")
    public ApiResponse<SuperAdminDashboardResponse> getDashboard() {

        SuperAdminDashboardResponse response =
                superAdminDashboardService.getDashboard();

        return ApiResponse.<SuperAdminDashboardResponse>builder()
                .success(true)
                .status(ApiStatus.SUCCESS)
                .message("Super Admin dashboard data retrieved successfully.")
                .data(response)
                .build();
    }
}