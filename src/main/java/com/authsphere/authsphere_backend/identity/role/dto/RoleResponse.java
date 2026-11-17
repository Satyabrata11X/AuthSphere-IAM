
package com.authsphere.authsphere_backend.identity.role.dto;

import java.util.List;
import java.util.UUID;

public record RoleResponse(
        UUID id,
        String name,
        String description,
        List<String> permissions
) {}
