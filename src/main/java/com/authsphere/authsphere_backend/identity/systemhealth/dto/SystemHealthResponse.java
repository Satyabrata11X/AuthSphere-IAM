package com.authsphere.authsphere_backend.identity.systemhealth.dto;


import java.time.Instant;
import java.util.List;

public record SystemHealthResponse(
        String overallStatus,
        Instant checkedAt,
        List<ComponentHealthResponse> components,
        SystemMetricsResponse metrics
) {}
