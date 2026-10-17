package com.authsphere.authsphere_backend.identity.role;

import com.authsphere.authsphere_backend.identity.role.dto.CreateRoleRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/roles")
@RequiredArgsConstructor
public class RoleController {

    private final RoleService roleService;

    // =========================================================
    // CREATE ROLE
    // =========================================================

    @PreAuthorize("hasAuthority('ROLE_CREATE')")
    @PostMapping
    public ResponseEntity<Role> createRole(
            @Valid @RequestBody CreateRoleRequest request
    ) {

        Role role =
                roleService.createRole(
                        request.name(),
                        request.description()
                );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(role);
    }


    // =========================================================
    // GET ALL ROLES
    // =========================================================

    @PreAuthorize("hasAuthority('ROLE_READ')")
    @GetMapping
    public ResponseEntity<List<Role>> getAllRoles() {

        return ResponseEntity.ok(
                roleService.getAllRoles()
        );
    }


    // =========================================================
    // GET ROLE
    // =========================================================

    @PreAuthorize("hasAuthority('ROLE_READ')")
    @GetMapping("/{roleId}")
    public ResponseEntity<Role> getRole(
            @PathVariable UUID roleId
    ) {

        return ResponseEntity.ok(
                roleService.getRole(roleId)
        );
    }


    // =========================================================
    // DELETE ROLE
    // =========================================================

    @PreAuthorize("hasAuthority('ROLE_DELETE')")
    @DeleteMapping("/{roleId}")
    public ResponseEntity<Void> deleteRole(
            @PathVariable UUID roleId
    ) {

        roleService.deleteRole(roleId);

        return ResponseEntity.noContent().build();
    }


    // =========================================================
    // ADD PERMISSION TO ROLE
    // =========================================================

    @PreAuthorize("hasAuthority('ROLE_UPDATE')")
    @PostMapping("/{roleId}/permissions/{permissionId}")
    public ResponseEntity<Role> addPermission(
            @PathVariable UUID roleId,
            @PathVariable UUID permissionId
    ) {

        return ResponseEntity.ok(
                roleService.addPermission(
                        roleId,
                        permissionId
                )
        );
    }


    // =========================================================
    // REMOVE PERMISSION FROM ROLE
    // =========================================================

    @PreAuthorize("hasAuthority('ROLE_UPDATE')")
    @DeleteMapping("/{roleId}/permissions/{permissionId}")
    public ResponseEntity<Role> removePermission(
            @PathVariable UUID roleId,
            @PathVariable UUID permissionId
    ) {

        return ResponseEntity.ok(
                roleService.removePermission(
                        roleId,
                        permissionId
                )
        );
    }
}