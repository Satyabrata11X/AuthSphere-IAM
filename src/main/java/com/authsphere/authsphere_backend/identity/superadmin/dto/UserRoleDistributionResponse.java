package com.authsphere.authsphere_backend.identity.superadmin.dto;

public record UserRoleDistributionResponse(
        String roleName,
        long userCount,
        double percentage
) {
}