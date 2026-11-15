
package com.authsphere.authsphere_backend.identity.systemhealth.controller;

import com.authsphere.authsphere_backend.identity.systemhealth.dto.SystemHealthResponse;
import com.authsphere.authsphere_backend.identity.systemhealth.service.SystemHealthService;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/superadmin/system-health")
@RequiredArgsConstructor
public class SystemHealthController {

    private final SystemHealthService systemHealthService;

    // =====================================================
    // GET SYSTEM HEALTH
    // =====================================================

    @GetMapping
    public ResponseEntity<SystemHealthResponse> getSystemHealth() {

        SystemHealthResponse response =
                systemHealthService.getSystemHealth();

        return ResponseEntity.ok(response);
    }
}
