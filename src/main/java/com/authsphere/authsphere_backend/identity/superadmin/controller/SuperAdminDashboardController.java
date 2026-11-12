
package com.authsphere.authsphere_backend.identity.superadmin.controller;

import com.authsphere.authsphere_backend.core.common.PageResponse;
import com.authsphere.authsphere_backend.identity.superadmin.dto.*;
import com.authsphere.authsphere_backend.identity.superadmin.service.SuperAdminDashboardService;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/superadmin/dashboard")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class SuperAdminDashboardController {

    private final SuperAdminDashboardService superAdminDashboardService;

    // Dashboard overview
    @GetMapping
    public ResponseEntity<SuperAdminDashboardResponse> getDashboard() {

        return ResponseEntity.ok(
                superAdminDashboardService.getDashboard()
        );
    }

    // User growth analytics
    @GetMapping("/analytics/user-growth")
    public ResponseEntity<List<UserGrowthResponse>> getUserGrowth(
            @RequestParam(defaultValue = "7") int days
    ) {
        return ResponseEntity.ok(
                superAdminDashboardService.getUserGrowth(days)
        );
    }

    // User distribution by role
    @GetMapping("/analytics/users-by-role")
    public ResponseEntity<List<UserRoleDistributionResponse>> getUsersByRole() {

        return ResponseEntity.ok(
                superAdminDashboardService.getUsersByRole()
        );
    }

    // Authentication activity analytics
    @GetMapping("/analytics/authentication-activity")
    public ResponseEntity<List<AuthenticationActivityResponse>>
    getAuthenticationActivity(
            @RequestParam(defaultValue = "7") int days
    ) {
        return ResponseEntity.ok(
                superAdminDashboardService.getAuthenticationActivity(days)
        );
    }

    // Recent security activity with pagination
    @GetMapping("/activity")
    public ResponseEntity<PageResponse<RecentActivityResponse>> getRecentActivity(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(
                superAdminDashboardService.getRecentActivity(page, size)
        );
    }
}
