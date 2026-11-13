
package com.authsphere.authsphere_backend.identity.superadmin.dto.user;

import com.authsphere.authsphere_backend.identity.user.AccountStatus;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

public record SuperAdminUserResponse(

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

        String companyName,

        LocalDateTime lastLoginAt,

        LocalDateTime createdAt

) {
}
