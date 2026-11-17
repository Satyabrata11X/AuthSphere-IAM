
package com.authsphere.authsphere_backend.identity.role.mapper;

import com.authsphere.authsphere_backend.identity.role.Role;
import com.authsphere.authsphere_backend.identity.role.dto.RoleResponse;
import org.springframework.stereotype.Component;

@Component
public class RoleMapper {

    public RoleResponse toResponse(Role role) {

        return new RoleResponse(
                role.getId(),
                role.getName(),
                role.getDescription(),
                role.getPermissions().stream()
                        .map(permission -> permission.getName())
                        .sorted()
                        .toList()
        );
    }
}
