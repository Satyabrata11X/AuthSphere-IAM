package com.authsphere.authsphere_backend.identity.permission;

import com.authsphere.authsphere_backend.identity.permission.dto.CreatePermissionRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/permissions")
@RequiredArgsConstructor
public class PermissionController {

    private final PermissionService permissionService;

    // =========================================================
    // CREATE PERMISSION
    // =========================================================

    @PreAuthorize("hasAuthority('PERMISSION_CREATE')")
    @PostMapping
    public ResponseEntity<Permission> createPermission(
            @Valid @RequestBody CreatePermissionRequest request
    ) {

        Permission permission =
                permissionService.createPermission(
                        request.name(),
                        request.description()
                );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(permission);
    }


    // =========================================================
    // GET ALL PERMISSIONS
    // =========================================================

    @PreAuthorize("hasAuthority('PERMISSION_READ')")
    @GetMapping
    public ResponseEntity<List<Permission>> getAllPermissions() {

        return ResponseEntity.ok(
                permissionService.getAllPermissions()
        );
    }


    // =========================================================
    // GET PERMISSION
    // =========================================================

    @PreAuthorize("hasAuthority('PERMISSION_READ')")
    @GetMapping("/{permissionId}")
    public ResponseEntity<Permission> getPermission(
            @PathVariable UUID permissionId
    ) {

        return ResponseEntity.ok(
                permissionService.getPermission(permissionId)
        );
    }


    // =========================================================
    // DELETE PERMISSION
    // =========================================================

    @PreAuthorize("hasAuthority('PERMISSION_DELETE')")
    @DeleteMapping("/{permissionId}")
    public ResponseEntity<Void> deletePermission(
            @PathVariable UUID permissionId
    ) {

        permissionService.deletePermission(
                permissionId
        );

        return ResponseEntity.noContent().build();
    }
}