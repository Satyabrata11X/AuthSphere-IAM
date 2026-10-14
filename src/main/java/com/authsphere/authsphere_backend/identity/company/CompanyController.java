
package com.authsphere.authsphere_backend.identity.company;

import com.authsphere.authsphere_backend.identity.user.User;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/companies")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class CompanyController {

    private final CompanyService companyService;

    // =========================================================
    // CREATE COMPANY
    // =========================================================

    @PostMapping
    public ResponseEntity<CompanyResponse> createCompany(
            @RequestParam String name,
            @RequestParam(required = false) String description,
            @RequestParam(required = false) String website,
            @RequestParam(required = false) String industry
    ) {

        Company company = companyService.createCompany(
                name,
                description,
                website,
                industry
        );

        return ResponseEntity.ok(toResponse(company));
    }

    // =========================================================
    // GET ALL COMPANIES — SUPER ADMIN
    // =========================================================

    @GetMapping
    public ResponseEntity<List<CompanyResponse>> getAllCompanies() {

        List<CompanyResponse> companies =
                companyService.getAllCompanies()
                        .stream()
                        .map(this::toResponse)
                        .toList();

        return ResponseEntity.ok(companies);
    }

    // =========================================================
    // GET AUTHENTICATED USER'S COMPANY
    // =========================================================


    @GetMapping("/my-company")
    public ResponseEntity<CompanyResponse> getMyCompany(
            Authentication authentication
    ) {
        if (authentication == null
                || !(authentication.getPrincipal() instanceof User user)) {
            return ResponseEntity.status(401).build();
        }

        if (user.getCompany() == null) {
            return ResponseEntity.notFound().build();
        }

        Company company = companyService.getCompany(
                user.getCompany().getId()
        );

        return ResponseEntity.ok(toResponse(company));
    }

    // =========================================================
    // GET COMPANY BY ID — SUPER ADMIN
    // =========================================================

    @GetMapping("/{companyId}")
    public ResponseEntity<CompanyResponse> getCompany(
            @PathVariable UUID companyId
    ) {

        Company company = companyService.getCompany(companyId);

        return ResponseEntity.ok(toResponse(company));
    }

    // =========================================================
    // UPDATE COMPANY — SUPER ADMIN
    // =========================================================

    @PutMapping("/{companyId}")
    public ResponseEntity<CompanyResponse> updateCompany(
            @PathVariable UUID companyId,
            @RequestParam String name,
            @RequestParam(required = false) String description,
            @RequestParam(required = false) String website,
            @RequestParam(required = false) String industry
    ) {

        Company company = companyService.updateCompany(
                companyId,
                name,
                description,
                website,
                industry
        );

        return ResponseEntity.ok(toResponse(company));
    }

    // =========================================================
    // UPDATE COMPANY STATUS — SUPER ADMIN
    // =========================================================

    @PatchMapping("/{companyId}/status")
    public ResponseEntity<CompanyResponse> updateCompanyStatus(
            @PathVariable UUID companyId,
            @RequestParam CompanyStatus status
    ) {

        Company company = companyService.updateCompanyStatus(
                companyId,
                status
        );

        return ResponseEntity.ok(toResponse(company));
    }

    // =========================================================
    // DELETE COMPANY — SUPER ADMIN
    // =========================================================

    @DeleteMapping("/{companyId}")
    public ResponseEntity<Void> deleteCompany(
            @PathVariable UUID companyId
    ) {

        companyService.deleteCompany(companyId);

        return ResponseEntity.noContent().build();
    }

    // =========================================================
    // ENTITY → RESPONSE DTO
    // =========================================================

    private CompanyResponse toResponse(Company company) {

        return new CompanyResponse(
                company.getId(),
                company.getName(),
                company.getDescription(),
                company.getWebsite(),
                company.getIndustry(),
                company.getStatus(),
                company.getCreatedAt(),
                company.getUpdatedAt()
        );
    }
}
