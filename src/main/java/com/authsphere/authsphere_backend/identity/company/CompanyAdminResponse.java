package com.authsphere.authsphere_backend.identity.company;

import com.authsphere.authsphere_backend.identity.user.AccountStatus;

import java.util.UUID;

public record CompanyAdminResponse(

        UUID id,

        String firstName,

        String lastName,

        String email,

        String employeeId,

        boolean emailVerified,

        boolean mfaEnabled,

        AccountStatus accountStatus,

        UUID companyId

) {
}