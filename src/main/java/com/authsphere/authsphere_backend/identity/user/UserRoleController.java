
package com.authsphere.authsphere_backend.identity.user;

import com.authsphere.authsphere_backend.identity.user.dto.UserRoleResponse;
import com.authsphere.authsphere_backend.identity.user.mapper.UserRoleMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserRoleController {

    private final UserRoleService userRoleService;
    private final UserRoleMapper userRoleMapper;

    // ASSIGN ROLE

    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @PostMapping("/{userId}/roles/{roleId}")
    public ResponseEntity<UserRoleResponse> assignRole(
            @PathVariable UUID userId,
            @PathVariable UUID roleId
    ) {
        User user = userRoleService.assignRole(userId, roleId);

        return ResponseEntity.ok(
                userRoleMapper.toResponse(user)
        );
    }

    // REMOVE ROLE

    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @DeleteMapping("/{userId}/roles/{roleId}")
    public ResponseEntity<UserRoleResponse> removeRole(
            @PathVariable UUID userId,
            @PathVariable UUID roleId
    ) {
        User user = userRoleService.removeRole(userId, roleId);

        return ResponseEntity.ok(
                userRoleMapper.toResponse(user)
        );
    }

    // GET USER ROLES

    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @GetMapping("/{userId}/roles")
    public ResponseEntity<UserRoleResponse> getUserRoles(
            @PathVariable UUID userId
    ) {
        User user = userRoleService.getUserWithRoles(userId);

        return ResponseEntity.ok(
                userRoleMapper.toResponse(user)
        );
    }
}
