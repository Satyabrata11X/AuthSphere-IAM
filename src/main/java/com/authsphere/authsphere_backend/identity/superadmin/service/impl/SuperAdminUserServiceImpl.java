
package com.authsphere.authsphere_backend.identity.superadmin.service.impl;

import com.authsphere.authsphere_backend.core.common.PageResponse;
import com.authsphere.authsphere_backend.identity.superadmin.dto.user.SuperAdminUserDetailsResponse;
import com.authsphere.authsphere_backend.identity.superadmin.dto.user.SuperAdminUserResponse;
import com.authsphere.authsphere_backend.identity.superadmin.service.SuperAdminUserService;
import com.authsphere.authsphere_backend.identity.user.AccountStatus;
import com.authsphere.authsphere_backend.identity.user.User;
import com.authsphere.authsphere_backend.identity.user.UserRepository;
import com.authsphere.authsphere_backend.identity.session.SessionService;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SuperAdminUserServiceImpl implements SuperAdminUserService {

    private final UserRepository userRepository;
    private final SessionService sessionService;

    // =========================================================
    // GET USERS WITH SEARCH, FILTERING AND PAGINATION
    // =========================================================

    @Override
    public PageResponse<SuperAdminUserResponse> getUsers(
            String search,
            AccountStatus accountStatus,
            String roleName,
            int page,
            int size
    ) {

        if (page < 0) {
            throw new IllegalArgumentException(
                    "Page number cannot be negative."
            );
        }

        if (size < 1 || size > 100) {
            throw new IllegalArgumentException(
                    "Page size must be between 1 and 100."
            );
        }

        // Use an empty string instead of null for search.
        String normalizedSearch =
                (search == null || search.isBlank())
                        ? ""
                        : search.trim();

        String normalizedRole =
                (roleName == null || roleName.isBlank())
                        ? null
                        : roleName.trim().toUpperCase();

        Pageable pageable = PageRequest.of(page, size);

        Page<User> userPage =
                userRepository.searchSuperAdminUsers(
                        normalizedSearch,
                        accountStatus,
                        normalizedRole,
                        pageable
                );

        var content = userPage.getContent()
                .stream()
                .map(this::mapToUserResponse)
                .toList();

        return PageResponse.<SuperAdminUserResponse>builder()
                .content(content)
                .page(userPage.getNumber())
                .size(userPage.getSize())
                .totalElements(userPage.getTotalElements())
                .totalPages(userPage.getTotalPages())
                .first(userPage.isFirst())
                .last(userPage.isLast())
                .build();
    }

    // =========================================================
    // GET INDIVIDUAL USER DETAILS
    // =========================================================

    @Override
    public SuperAdminUserDetailsResponse getUserDetails(UUID userId) {

        User user = userRepository
                .findActiveUserDetailsById(userId)
                .orElseThrow(() ->
                        new IllegalArgumentException("User not found.")
                );

        return mapToUserDetailsResponse(user);
    }

    // =========================================================
    // MAP USER TO LIST RESPONSE
    // =========================================================

    private SuperAdminUserResponse mapToUserResponse(User user) {

        Set<String> roles = user.getRoles()
                .stream()
                .map(role -> role.getName())
                .collect(Collectors.toSet());

        String companyName =
                user.getCompany() != null
                        ? user.getCompany().getName()
                        : null;

        return new SuperAdminUserResponse(
                user.getId(),
                user.getFirstName(),
                user.getLastName(),
                user.getEmail(),
                user.getEmployeeId(),
                user.getPhoneNumber(),
                user.getProfileImageUrl(),
                user.getAccountStatus(),
                user.isEmailVerified(),
                user.isMfaEnabled(),
                roles,
                companyName,
                user.getLastLoginAt(),
                user.getCreatedAt()
        );
    }

    // =========================================================
    // MAP USER TO DETAILS RESPONSE
    // =========================================================

    private SuperAdminUserDetailsResponse mapToUserDetailsResponse(
            User user
    ) {

        Set<String> roles = user.getRoles()
                .stream()
                .map(role -> role.getName())
                .collect(Collectors.toSet());

        UUID companyId = null;
        String companyName = null;

        if (user.getCompany() != null) {
            companyId = user.getCompany().getId();
            companyName = user.getCompany().getName();
        }

        UUID managedById = null;
        String managedByEmail = null;

        if (user.getManagedBy() != null) {
            managedById = user.getManagedBy().getId();
            managedByEmail = user.getManagedBy().getEmail();
        }

        return new SuperAdminUserDetailsResponse(
                user.getId(),
                user.getFirstName(),
                user.getLastName(),
                user.getEmail(),
                user.getEmployeeId(),
                user.getPhoneNumber(),
                user.getProfileImageUrl(),
                user.getAccountStatus(),
                user.isEmailVerified(),
                user.isMfaEnabled(),
                roles,
                companyId,
                companyName,
                managedById,
                managedByEmail,
                user.getLastLoginAt(),
                user.getLockedUntil(),
                user.getFailedLoginAttempts(),
                user.getLockoutCount(),
                user.getCreatedAt(),
                user.getUpdatedAt()
        );
    }

    // =========================================================
    // SUSPEND USER
    // =========================================================

    @Override
    @Transactional
    public void suspendUser(UUID userId, UUID superAdminId) {

        User user = findUserForManagement(userId);

        preventSuperAdminModification(user, superAdminId);

        if (user.getAccountStatus() == AccountStatus.SUSPENDED) {
            throw new IllegalStateException(
                    "User is already suspended."
            );
        }

        if (user.getAccountStatus() != AccountStatus.ACTIVE) {
            throw new IllegalStateException(
                    "Only active users can be suspended."
            );
        }

        user.setAccountStatus(AccountStatus.SUSPENDED);

        userRepository.save(user);

        // Revoke all active sessions.
        sessionService.revokeAllSessions(user);
    }

    // =========================================================
    // ACTIVATE USER
    // =========================================================

    @Override
    @Transactional
    public void activateUser(UUID userId, UUID superAdminId) {

        User user = findUserForManagement(userId);

        preventSuperAdminModification(user, superAdminId);

        if (user.getAccountStatus() != AccountStatus.SUSPENDED) {
            throw new IllegalStateException(
                    "Only suspended users can be activated."
            );
        }

        user.setAccountStatus(AccountStatus.ACTIVE);

        userRepository.save(user);
    }

    // =========================================================
    // SOFT DELETE USER
    // =========================================================

    @Override
    @Transactional
    public void deleteUser(UUID userId, UUID superAdminId) {

        User user = findUserForManagement(userId);

        preventSuperAdminModification(user, superAdminId);

        user.setDeleted(true);
        user.setDeletedAt(LocalDateTime.now());
        user.setDeletedBy(superAdminId);

        userRepository.save(user);

        // Revoke all active sessions.
        sessionService.revokeAllSessions(user);
    }

    // =========================================================
    // FIND USER FOR MANAGEMENT
    // =========================================================

    private User findUserForManagement(UUID userId) {

        return userRepository.findActiveUserDetailsById(userId)
                .orElseThrow(() ->
                        new IllegalArgumentException("User not found.")
                );
    }

    // =========================================================
    // PREVENT SUPERADMIN MODIFICATION
    // =========================================================

    private void preventSuperAdminModification(
            User targetUser,
            UUID superAdminId
    ) {

        if (targetUser.getId().equals(superAdminId)) {
            throw new IllegalStateException(
                    "You cannot modify your own SuperAdmin account."
            );
        }

        boolean isSuperAdmin = targetUser.getRoles()
                .stream()
                .anyMatch(role ->
                        "SUPER_ADMIN".equals(role.getName())
                );

        if (isSuperAdmin) {
            throw new IllegalStateException(
                    "SuperAdmin accounts cannot be modified through this operation."
            );
        }
    }
}
