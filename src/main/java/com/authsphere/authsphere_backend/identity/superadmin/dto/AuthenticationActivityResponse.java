package com.authsphere.authsphere_backend.identity.superadmin.dto;

import java.time.LocalDate;

public record AuthenticationActivityResponse(
        LocalDate date,
        long successfulAttempts,
        long failedAttempts
) {
}