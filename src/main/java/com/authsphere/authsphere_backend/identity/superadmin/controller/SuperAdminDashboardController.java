package com.authsphere.authsphere_backend.identity.superadmin.controller;


import com.authsphere.authsphere_backend.identity.superadmin.dto.SuperAdminDashboardResponse;
import com.authsphere.authsphere_backend.identity.superadmin.service.SuperAdminDashboardService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/superadmin/dashboard")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class SuperAdminDashboardController {

    private final SuperAdminDashboardService superAdminDashboardService;

    @GetMapping
    public ResponseEntity<SuperAdminDashboardResponse> getDashboard() {

        return ResponseEntity.ok(
                superAdminDashboardService.getDashboard()
        );
    }
}