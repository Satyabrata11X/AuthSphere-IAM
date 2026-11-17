
package com.authsphere.authsphere_backend.identity.user.dto;

import com.authsphere.authsphere_backend.identity.user.AccountStatus;

import java.util.List;
import java.util.UUID;

public record UserRoleResponse(
        UUID id,
        String firstName,
        String lastName,
        String email,
        String employeeId,
        AccountStatus accountStatus,
        List<String> roles
) {
}
