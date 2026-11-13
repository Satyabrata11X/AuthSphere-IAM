
package com.authsphere.authsphere_backend.identity.superadmin.service;

import com.authsphere.authsphere_backend.core.common.PageResponse;
import com.authsphere.authsphere_backend.identity.superadmin.dto.user.SuperAdminUserDetailsResponse;
import com.authsphere.authsphere_backend.identity.superadmin.dto.user.SuperAdminUserResponse;
import com.authsphere.authsphere_backend.identity.user.AccountStatus;

import java.util.UUID;

public interface SuperAdminUserService {

    PageResponse<SuperAdminUserResponse> getUsers(
            String search,
            AccountStatus accountStatus,
            String roleName,
            int page,
            int size
    );

    SuperAdminUserDetailsResponse getUserDetails(UUID userId);

    void suspendUser(UUID userId, UUID superAdminId);

    void activateUser(UUID userId, UUID superAdminId);

    void deleteUser(UUID userId, UUID superAdminId);
}
