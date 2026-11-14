package com.authsphere.authsphere_backend.identity.superadmin.dto.company;

import com.authsphere.authsphere_backend.identity.company.CompanyStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder
public class SuperAdminCompanyDetailsResponse {

    private UUID companyId;
    private String name;
    private String description;
    private String website;
    private String industry;
    private CompanyStatus status;

    private long totalUsers;
    private long activeUsers;
    private long totalAdmins;
    private long activeSessions;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}