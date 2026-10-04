package com.authsphere.authsphere_backend.identity.superadmin.dto;

public record SuperAdminDashboardResponse(

        long totalCompanies,

        long activeCompanies,

        long totalUsers,

        long companyUsers,

        long totalAdministrators,

        long activeUsers,

        long lockedUsers,

        long suspendedUsers,

        long mfaEnabledUsers,

        long activeSessions,

        long totalSecurityEvents

) {
}