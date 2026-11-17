
package com.authsphere.authsphere_backend.identity.role;

import com.authsphere.authsphere_backend.identity.role.dto.CreateRoleRequest;
import com.authsphere.authsphere_backend.identity.role.dto.RoleResponse;
import com.authsphere.authsphere_backend.identity.role.mapper.RoleMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/roles")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class RoleController {

    private final RoleService roleService;
    private final RoleMapper roleMapper;

    // CREATE ROLE

    @PreAuthorize("hasAuthority('ROLE_CREATE')")
    @PostMapping
    public ResponseEntity<RoleResponse> createRole(
            @Valid @RequestBody CreateRoleRequest request
    ) {
        Role role = roleService.createRole(
                request.name(),
                request.description()
        );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(roleMapper.toResponse(role));
    }

    // GET ALL ROLES

    @PreAuthorize("hasAuthority('ROLE_READ')")
    @GetMapping
    public ResponseEntity<List<RoleResponse>> getAllRoles() {

        List<RoleResponse> response = roleService.getAllRoles()
                .stream()
                .map(roleMapper::toResponse)
                .toList();

        return ResponseEntity.ok(response);
    }

    // GET ROLE BY ID

    @PreAuthorize("hasAuthority('ROLE_READ')")
    @GetMapping("/{roleId}")
    public ResponseEntity<RoleResponse> getRole(
            @PathVariable UUID roleId
    ) {
        Role role = roleService.getRole(roleId);

        return ResponseEntity.ok(
                roleMapper.toResponse(role)
        );
    }

    // DELETE ROLE

    @PreAuthorize("hasAuthority('ROLE_DELETE')")
    @DeleteMapping("/{roleId}")
    public ResponseEntity<Void> deleteRole(
            @PathVariable UUID roleId
    ) {
        roleService.deleteRole(roleId);

        return ResponseEntity.noContent().build();
    }

    // ADD PERMISSION TO ROLE

    @PreAuthorize("hasAuthority('ROLE_UPDATE')")
    @PostMapping("/{roleId}/permissions/{permissionId}")
    public ResponseEntity<RoleResponse> addPermission(
            @PathVariable UUID roleId,
            @PathVariable UUID permissionId
    ) {
        Role role = roleService.addPermission(roleId, permissionId);

        return ResponseEntity.ok(
                roleMapper.toResponse(role)
        );
    }

    // REMOVE PERMISSION FROM ROLE

    @PreAuthorize("hasAuthority('ROLE_UPDATE')")
    @DeleteMapping("/{roleId}/permissions/{permissionId}")
    public ResponseEntity<RoleResponse> removePermission(
            @PathVariable UUID roleId,
            @PathVariable UUID permissionId
    ) {
        Role role = roleService.removePermission(roleId, permissionId);

        return ResponseEntity.ok(
                roleMapper.toResponse(role)
        );
    }
}
