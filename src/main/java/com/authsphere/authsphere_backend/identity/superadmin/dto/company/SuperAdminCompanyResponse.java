package com.authsphere.authsphere_backend.identity.superadmin.dto.company;

import com.authsphere.authsphere_backend.identity.company.CompanyStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder
public class SuperAdminCompanyResponse {

    private UUID companyId;
    private String name;
    private String industry;
    private String website;
    private CompanyStatus status;

    private long totalUsers;
    private long totalAdmins;

    private LocalDateTime createdAt;
}