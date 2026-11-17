
package com.authsphere.authsphere_backend.identity.permission.mapper;

import com.authsphere.authsphere_backend.identity.permission.Permission;
import com.authsphere.authsphere_backend.identity.permission.dto.PermissionResponse;
import org.springframework.stereotype.Component;

@Component
public class PermissionMapper {

    public PermissionResponse toResponse(Permission permission) {

        return new PermissionResponse(
                permission.getId(),
                permission.getName(),
                permission.getDescription()
        );
    }
}
