package com.authsphere.authsphere_backend.identity.superadmin.service.impl;

import com.authsphere.authsphere_backend.core.common.PageResponse;
import com.authsphere.authsphere_backend.core.exception.ResourceNotFoundException;
import com.authsphere.authsphere_backend.identity.company.Company;
import com.authsphere.authsphere_backend.identity.company.CompanyRepository;
import com.authsphere.authsphere_backend.identity.company.CompanyService;
import com.authsphere.authsphere_backend.identity.company.CompanyStatus;
import com.authsphere.authsphere_backend.identity.session.SessionRepository;
import com.authsphere.authsphere_backend.identity.session.SessionStatus;
import com.authsphere.authsphere_backend.identity.superadmin.dto.company.SuperAdminCompanyDetailsResponse;
import com.authsphere.authsphere_backend.identity.superadmin.dto.company.SuperAdminCompanyResponse;
import com.authsphere.authsphere_backend.identity.superadmin.dto.company.SuperAdminCompanyUserResponse;
import com.authsphere.authsphere_backend.identity.superadmin.service.SuperAdminCompanyService;
import com.authsphere.authsphere_backend.identity.user.AccountStatus;
import com.authsphere.authsphere_backend.identity.user.User;
import com.authsphere.authsphere_backend.identity.user.UserRepository;
import com.authsphere.authsphere_backend.core.exception.BusinessConflictException;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class SuperAdminCompanyServiceImpl
        implements SuperAdminCompanyService {

    private final CompanyRepository companyRepository;
    private final UserRepository userRepository;
    private final SessionRepository sessionRepository;
    private final CompanyService companyService;

    // =========================================================
    // GET ALL COMPANIES
    // =========================================================

    @Override
    @Transactional(readOnly = true)
    public PageResponse<SuperAdminCompanyResponse> getCompanies(
            String search,
            CompanyStatus status,
            int page,
            int size
    ) {

        validatePagination(page, size);

        String normalizedSearch =
                search == null ? "" : search.trim();

        Pageable pageable = PageRequest.of(page, size);

        Page<Company> companies =
                companyRepository.searchSuperAdminCompanies(
                        normalizedSearch,
                        status,
                        pageable
                );

        Page<SuperAdminCompanyResponse> responsePage =
                companies.map(this::toCompanyResponse);

        return toPageResponse(responsePage);
    }

    // =========================================================
    // GET COMPANY DETAILS
    // =========================================================

    @Override
    @Transactional(readOnly = true)
    public SuperAdminCompanyDetailsResponse getCompanyDetails(
            UUID companyId
    ) {

        Company company = getActiveCompany(companyId);

        long totalUsers =
                userRepository.countActiveCompanyRecords(companyId);

        long activeUsers =
                userRepository.countCompanyUsersByStatus(
                        companyId,
                        AccountStatus.ACTIVE
                );

        long totalAdmins =
                userRepository.countActiveCompanyAdministrators(
                        companyId
                );

        long activeSessions =
                sessionRepository.countValidCompanySessions(
                        companyId,
                        SessionStatus.ACTIVE,
                        LocalDateTime.now()
                );

        return SuperAdminCompanyDetailsResponse.builder()
                .companyId(company.getId())
                .name(company.getName())
                .description(company.getDescription())
                .website(company.getWebsite())
                .industry(company.getIndustry())
                .status(company.getStatus())
                .totalUsers(totalUsers)
                .activeUsers(activeUsers)
                .totalAdmins(totalAdmins)
                .activeSessions(activeSessions)
                .createdAt(company.getCreatedAt())
                .updatedAt(company.getUpdatedAt())
                .build();
    }

    // =========================================================
    // GET COMPANY USERS
    // =========================================================

    @Override
    @Transactional(readOnly = true)
    public PageResponse<SuperAdminCompanyUserResponse> getCompanyUsers(
            UUID companyId,
            int page,
            int size
    ) {

        validatePagination(page, size);

        getActiveCompany(companyId);

        Pageable pageable = PageRequest.of(page, size);

        Page<User> users =
                userRepository.findSuperAdminCompanyUsers(
                        companyId,
                        pageable
                );

        Page<SuperAdminCompanyUserResponse> responsePage =
                users.map(this::toCompanyUserResponse);

        return toPageResponse(responsePage);
    }

    // =========================================================
    // SUSPEND COMPANY
    // =========================================================


    @Override
    public void suspendCompany(UUID companyId) {

        Company company = getActiveCompany(companyId);

        if (company.getStatus() == CompanyStatus.SUSPENDED) {
            throw new BusinessConflictException(
                    "Company is already suspended."
            );
        }

        companyService.updateCompanyStatus(
                companyId,
                CompanyStatus.SUSPENDED
        );
    }

    // =========================================================
    // ACTIVATE COMPANY
    // =========================================================

    @Override
    public void activateCompany(UUID companyId) {

        Company company = getActiveCompany(companyId);

        if (company.getStatus() == CompanyStatus.ACTIVE) {
            throw new BusinessConflictException(
                    "Company is already active."
            );
        }

        companyService.updateCompanyStatus(
                companyId,
                CompanyStatus.ACTIVE
        );
    }

    // =========================================================
    // HELPER METHODS
    // =========================================================

    private Company getActiveCompany(UUID companyId) {

        return companyRepository.findById(companyId)
                .filter(company -> !company.isDeleted())
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Company not found: " + companyId
                        )
                );
    }

    private SuperAdminCompanyResponse toCompanyResponse(
            Company company
    ) {

        UUID companyId = company.getId();

        return SuperAdminCompanyResponse.builder()
                .companyId(companyId)
                .name(company.getName())
                .industry(company.getIndustry())
                .website(company.getWebsite())
                .status(company.getStatus())
                .totalUsers(
                        userRepository.countActiveCompanyRecords(companyId)
                )
                .totalAdmins(
                        userRepository.countActiveCompanyAdministrators(companyId)
                )
                .createdAt(company.getCreatedAt())
                .build();
    }

    private SuperAdminCompanyUserResponse toCompanyUserResponse(
            User user
    ) {

        return SuperAdminCompanyUserResponse.builder()
                .userId(user.getId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .employeeId(user.getEmployeeId())
                .accountStatus(user.getAccountStatus())
                .emailVerified(user.isEmailVerified())
                .mfaEnabled(user.isMfaEnabled())
                .createdAt(user.getCreatedAt())
                .build();
    }

    private <T> PageResponse<T> toPageResponse(Page<T> page) {

        return PageResponse.<T>builder()
                .content(page.getContent())
                .page(page.getNumber())
                .size(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .first(page.isFirst())
                .last(page.isLast())
                .build();
    }

    private void validatePagination(int page, int size) {

        if (page < 0 || size < 1 || size > 100) {
            throw new IllegalArgumentException(
                    "Invalid pagination parameters. Page must be >= 0 "
                            + "and size must be between 1 and 100."
            );
        }
    }
}