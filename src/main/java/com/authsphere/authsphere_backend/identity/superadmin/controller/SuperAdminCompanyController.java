
package com.authsphere.authsphere_backend.identity.superadmin.controller;

import com.authsphere.authsphere_backend.core.common.PageResponse;
import com.authsphere.authsphere_backend.identity.company.CompanyStatus;
import com.authsphere.authsphere_backend.identity.superadmin.dto.company.SuperAdminCompanyDetailsResponse;
import com.authsphere.authsphere_backend.identity.superadmin.dto.company.SuperAdminCompanyResponse;
import com.authsphere.authsphere_backend.identity.superadmin.dto.company.SuperAdminCompanyUserResponse;
import com.authsphere.authsphere_backend.identity.superadmin.service.SuperAdminCompanyService;
import com.authsphere.authsphere_backend.core.common.ApiResponse;
import com.authsphere.authsphere_backend.core.common.ApiStatus;
import org.springframework.http.HttpStatus;



import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/superadmin/companies")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class SuperAdminCompanyController {

    private final SuperAdminCompanyService superAdminCompanyService;

    // =========================================================
    // GET ALL COMPANIES
    // =========================================================

    @GetMapping
    public ResponseEntity<PageResponse<SuperAdminCompanyResponse>> getCompanies(

            @RequestParam(required = false)
            String search,

            @RequestParam(required = false)
            CompanyStatus status,

            @RequestParam(defaultValue = "0")
            int page,

            @RequestParam(defaultValue = "10")
            int size

    ) {

        return ResponseEntity.ok(
                superAdminCompanyService.getCompanies(
                        search,
                        status,
                        page,
                        size
                )
        );
    }

    // =========================================================
    // GET COMPANY DETAILS
    // =========================================================

    @GetMapping("/{companyId}")
    public ResponseEntity<SuperAdminCompanyDetailsResponse> getCompanyDetails(
            @PathVariable UUID companyId
    ) {

        return ResponseEntity.ok(
                superAdminCompanyService.getCompanyDetails(companyId)
        );
    }

    // =========================================================
    // GET COMPANY EMPLOYEES
    // =========================================================

    @GetMapping("/{companyId}/users")
    public ResponseEntity<PageResponse<SuperAdminCompanyUserResponse>> getCompanyUsers(

            @PathVariable UUID companyId,

            @RequestParam(defaultValue = "0")
            int page,

            @RequestParam(defaultValue = "10")
            int size

    ) {

        return ResponseEntity.ok(
                superAdminCompanyService.getCompanyUsers(
                        companyId,
                        page,
                        size
                )
        );
    }


    // =========================================================
// SUSPEND COMPANY
// =========================================================

    @PatchMapping("/{companyId}/suspend")
    public ResponseEntity<ApiResponse<Void>> suspendCompany(
            @PathVariable UUID companyId
    ) {

        superAdminCompanyService.suspendCompany(companyId);

        ApiResponse<Void> response = ApiResponse.<Void>builder()
                .success(true)
                .status(ApiStatus.SUCCESS)
                .message("Company suspended successfully.")
                .data(null)
                .build();

        return ResponseEntity.ok(response);
    }

// =========================================================
// ACTIVATE COMPANY
// =========================================================

    @PatchMapping("/{companyId}/activate")
    public ResponseEntity<ApiResponse<Void>> activateCompany(
            @PathVariable UUID companyId
    ) {

        superAdminCompanyService.activateCompany(companyId);

        ApiResponse<Void> response = ApiResponse.<Void>builder()
                .success(true)
                .status(ApiStatus.SUCCESS)
                .message("Company activated successfully.")
                .data(null)
                .build();

        return ResponseEntity.ok(response);
    }
}
