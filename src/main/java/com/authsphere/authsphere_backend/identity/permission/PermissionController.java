package com.authsphere.authsphere_backend.identity.permission;

import com.authsphere.authsphere_backend.core.common.ApiResponse;
import com.authsphere.authsphere_backend.identity.permission.dto.PermissionCreateRequest;
import com.authsphere.authsphere_backend.identity.permission.dto.PermissionResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/permissions")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class PermissionController {

    private final PermissionService permissionService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<PermissionResponse> createPermission(
            @Valid @RequestBody PermissionCreateRequest request) {

        Permission permission = permissionService.createPermission(
                request.getName(),
                request.getDescription()
        );

        PermissionResponse response = PermissionResponse.builder()
                .id(permission.getId())
                .name(permission.getName())
                .description(permission.getDescription())
                .build();

        return ApiResponse.<PermissionResponse>builder()
                .success(true)
                .message("Permission created successfully.")
                .data(response)
                .build();
    }

    @GetMapping
    public ApiResponse<List<PermissionResponse>> getAllPermissions() {

        List<PermissionResponse> permissions =
                permissionService.getAllPermissions()
                        .stream()
                        .map(permission -> PermissionResponse.builder()
                                .id(permission.getId())
                                .name(permission.getName())
                                .description(permission.getDescription())
                                .build())
                        .toList();

        return ApiResponse.<List<PermissionResponse>>builder()
                .success(true)
                .message("Permissions retrieved successfully.")
                .data(permissions)
                .build();
    }

    @GetMapping("/{id}")
    public ApiResponse<PermissionResponse> getPermission(
            @PathVariable UUID id) {

        Permission permission = permissionService.getPermissionById(id);

        PermissionResponse response = PermissionResponse.builder()
                .id(permission.getId())
                .name(permission.getName())
                .description(permission.getDescription())
                .build();

        return ApiResponse.<PermissionResponse>builder()
                .success(true)
                .message("Permission retrieved successfully.")
                .data(response)
                .build();
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> deletePermission(
            @PathVariable UUID id) {

        permissionService.deletePermission(id);

        return ApiResponse.<Void>builder()
                .success(true)
                .message("Permission deleted successfully.")
                .build();
    }
}