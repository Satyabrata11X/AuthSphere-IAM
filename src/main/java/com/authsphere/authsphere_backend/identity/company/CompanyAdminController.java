package com.authsphere.authsphere_backend.identity.company;

import com.authsphere.authsphere_backend.identity.user.User;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/companies")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class CompanyAdminController {

    private final CompanyAdminService companyAdminService;

    /*
     * =========================================================
     * ASSIGN USER AS COMPANY ADMIN
     * =========================================================
     */

    @PostMapping("/{companyId}/admins/{userId}")
    public ResponseEntity<CompanyAdminResponse> assignAdmin(
            @PathVariable UUID companyId,
            @PathVariable UUID userId
    ) {

        User user =
                companyAdminService.assignAdmin(
                        companyId,
                        userId
                );

        return ResponseEntity.ok(
                toResponse(user)
        );
    }

    /*
     * =========================================================
     * REMOVE USER FROM COMPANY ADMIN ROLE
     * =========================================================
     */

    @DeleteMapping("/{companyId}/admins/{userId}")
    public ResponseEntity<CompanyAdminResponse> removeAdmin(
            @PathVariable UUID companyId,
            @PathVariable UUID userId
    ) {

        User user =
                companyAdminService.removeAdmin(
                        companyId,
                        userId
                );

        return ResponseEntity.ok(
                toResponse(user)
        );
    }

    /*
     * =========================================================
     * GET COMPANY ADMINS
     * =========================================================
     */

    @GetMapping("/{companyId}/admins")
    public ResponseEntity<java.util.List<CompanyAdminResponse>> getCompanyAdmins(
            @PathVariable UUID companyId
    ) {

        java.util.List<CompanyAdminResponse> admins =
                companyAdminService
                        .getCompanyAdmins(companyId)
                        .stream()
                        .map(this::toResponse)
                        .toList();

        return ResponseEntity.ok(admins);
    }

    /*
     * =========================================================
     * ENTITY → SAFE RESPONSE
     * =========================================================
     */

    private CompanyAdminResponse toResponse(User user) {

        return new CompanyAdminResponse(
                user.getId(),
                user.getFirstName(),
                user.getLastName(),
                user.getEmail(),
                user.getEmployeeId(),
                user.isEmailVerified(),
                user.isMfaEnabled(),
                user.getAccountStatus(),
                user.getCompany() != null
                        ? user.getCompany().getId()
                        : null
        );
    }
}