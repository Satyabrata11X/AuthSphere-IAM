
package com.authsphere.authsphere_backend.identity.superadmin.dto.user;

import com.authsphere.authsphere_backend.identity.user.AccountStatus;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

public record SuperAdminUserDetailsResponse(

        UUID id,

        String firstName,

        String lastName,

        String email,

        String employeeId,

        String phoneNumber,

        String profileImageUrl,

        AccountStatus accountStatus,

        boolean emailVerified,

        boolean mfaEnabled,

        Set<String> roles,

        UUID companyId,

        String companyName,

        UUID managedById,

        String managedByEmail,

        LocalDateTime lastLoginAt,

        LocalDateTime lockedUntil,

        int failedLoginAttempts,

        int lockoutCount,

        LocalDateTime createdAt,

        LocalDateTime updatedAt

) {
}
