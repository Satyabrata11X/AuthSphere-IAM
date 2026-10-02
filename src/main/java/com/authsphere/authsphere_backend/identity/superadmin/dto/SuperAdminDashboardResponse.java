package com.authsphere.authsphere_backend.identity.superadmin.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SuperAdminDashboardResponse {

    private long totalUsers;

    private long totalAdministrators;

    private long activeSessions;

    private long mfaEnabledUsers;

    private long securityEvents;
}