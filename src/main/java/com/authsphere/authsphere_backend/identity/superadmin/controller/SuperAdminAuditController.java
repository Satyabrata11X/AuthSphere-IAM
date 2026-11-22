package com.authsphere.authsphere_backend.identity.superadmin.controller;

import com.authsphere.authsphere_backend.core.common.PageResponse;
import com.authsphere.authsphere_backend.identity.auth.security.SecurityEventType;
import com.authsphere.authsphere_backend.identity.superadmin.dto.AuditLogResponse;
import com.authsphere.authsphere_backend.identity.superadmin.service.SuperAdminAuditService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/superadmin/audit-logs")
@RequiredArgsConstructor
public class SuperAdminAuditController {

    private final SuperAdminAuditService superAdminAuditService;

    @GetMapping
    public PageResponse<AuditLogResponse> getAuditLogs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) SecurityEventType eventType,
            @RequestParam(required = false) Boolean success,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime startDate,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime endDate
    ) {
        return superAdminAuditService.getAuditLogs(
                page, size, eventType, success, startDate, endDate
        );
    }
}