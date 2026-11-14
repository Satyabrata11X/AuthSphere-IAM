
package com.authsphere.authsphere_backend.identity.superadmin.service;

import com.authsphere.authsphere_backend.core.common.PageResponse;
import com.authsphere.authsphere_backend.identity.company.CompanyStatus;
import com.authsphere.authsphere_backend.identity.superadmin.dto.company.SuperAdminCompanyDetailsResponse;
import com.authsphere.authsphere_backend.identity.superadmin.dto.company.SuperAdminCompanyResponse;
import com.authsphere.authsphere_backend.identity.superadmin.dto.company.SuperAdminCompanyUserResponse;

import java.util.UUID;

public interface SuperAdminCompanyService {

    PageResponse<SuperAdminCompanyResponse> getCompanies(
            String search,
            CompanyStatus status,
            int page,
            int size
    );

    SuperAdminCompanyDetailsResponse getCompanyDetails(
            UUID companyId
    );

    PageResponse<SuperAdminCompanyUserResponse> getCompanyUsers(
            UUID companyId,
            int page,
            int size
    );

    void suspendCompany(UUID companyId);

    void activateCompany(UUID companyId);
}
