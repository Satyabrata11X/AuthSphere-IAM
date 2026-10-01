package com.authsphere.authsphere_backend.identity.user;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserRoleController {

    private final UserRoleService userRoleService;

    // =========================================================
    // ASSIGN ROLE
    // =========================================================

    @PostMapping("/{userId}/roles/{roleId}")
    public ResponseEntity<User> assignRole(
            @PathVariable UUID userId,
            @PathVariable UUID roleId
    ) {

        return ResponseEntity.ok(
                userRoleService.assignRole(
                        userId,
                        roleId
                )
        );
    }


    // =========================================================
    // REMOVE ROLE
    // =========================================================

    @DeleteMapping("/{userId}/roles/{roleId}")
    public ResponseEntity<User> removeRole(
            @PathVariable UUID userId,
            @PathVariable UUID roleId
    ) {

        return ResponseEntity.ok(
                userRoleService.removeRole(
                        userId,
                        roleId
                )
        );
    }


    // =========================================================
    // GET USER + ROLES
    // =========================================================

    @GetMapping("/{userId}/roles")
    public ResponseEntity<User> getUserRoles(
            @PathVariable UUID userId
    ) {

        return ResponseEntity.ok(
                userRoleService.getUserWithRoles(userId)
        );
    }
}