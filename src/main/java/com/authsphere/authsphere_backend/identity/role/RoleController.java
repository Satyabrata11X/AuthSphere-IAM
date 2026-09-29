package com.authsphere.authsphere_backend.identity.role;

import com.authsphere.authsphere_backend.core.common.ApiResponse;
import com.authsphere.authsphere_backend.identity.role.dto.RoleCreateRequest;
import com.authsphere.authsphere_backend.identity.role.dto.RoleResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/roles")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class RoleController {

    private final RoleService roleService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<RoleResponse> createRole(
            @Valid @RequestBody RoleCreateRequest request) {

        Role role = roleService.createRole(
                request.getName(),
                request.getDescription()
        );

        RoleResponse response = RoleResponse.builder()
                .id(role.getId())
                .name(role.getName())
                .description(role.getDescription())
                .build();

        return ApiResponse.<RoleResponse>builder()
                .success(true)
                .message("Role created successfully.")
                .data(response)
                .build();
    }

    @GetMapping
    public ApiResponse<List<RoleResponse>> getAllRoles() {

        List<RoleResponse> roles =
                roleService.getAllRoles()
                        .stream()
                        .map(role -> RoleResponse.builder()
                                .id(role.getId())
                                .name(role.getName())
                                .description(role.getDescription())
                                .build())
                        .toList();

        return ApiResponse.<List<RoleResponse>>builder()
                .success(true)
                .message("Roles retrieved successfully.")
                .data(roles)
                .build();
    }

    @GetMapping("/{id}")
    public ApiResponse<RoleResponse> getRole(
            @PathVariable UUID id) {

        Role role = roleService.getRoleById(id);

        RoleResponse response = RoleResponse.builder()
                .id(role.getId())
                .name(role.getName())
                .description(role.getDescription())
                .build();

        return ApiResponse.<RoleResponse>builder()
                .success(true)
                .message("Role retrieved successfully.")
                .data(response)
                .build();
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteRole(
            @PathVariable UUID id) {

        roleService.deleteRole(id);

        return ApiResponse.<Void>builder()
                .success(true)
                .message("Role deleted successfully.")
                .build();
    }

    @PostMapping("/{roleId}/permissions/{permissionId}")
    public ApiResponse<RoleResponse> assignPermission(
            @PathVariable UUID roleId,
            @PathVariable UUID permissionId) {

        Role role = roleService.assignPermission(roleId, permissionId);

        RoleResponse response = RoleResponse.builder()
                .id(role.getId())
                .name(role.getName())
                .description(role.getDescription())
                .build();

        return ApiResponse.<RoleResponse>builder()
                .success(true)
                .message("Permission assigned to role successfully.")
                .data(response)
                .build();
    }
}