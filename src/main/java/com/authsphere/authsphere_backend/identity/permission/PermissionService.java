package com.authsphere.authsphere_backend.identity.permission;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class PermissionService {

    private final PermissionRepository permissionRepository;


    // =========================================================
    // CREATE PERMISSION
    // =========================================================

    public Permission createPermission(
            String name,
            String description
    ) {

        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException(
                    "Permission name is required"
            );
        }

        String normalizedName =
                name.trim().toUpperCase();

        if (permissionRepository.existsByName(normalizedName)) {
            throw new IllegalArgumentException(
                    "Permission already exists: "
                            + normalizedName
            );
        }

        Permission permission =
                Permission.builder()
                        .name(normalizedName)
                        .description(description)
                        .build();

        return permissionRepository.save(permission);
    }


    // =========================================================
    // GET ALL PERMISSIONS
    // =========================================================

    @Transactional(readOnly = true)
    public List<Permission> getAllPermissions() {

        return permissionRepository.findAll();
    }


    // =========================================================
    // GET PERMISSION
    // =========================================================

    @Transactional(readOnly = true)
    public Permission getPermission(UUID permissionId) {

        return permissionRepository.findById(permissionId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Permission not found: "
                                        + permissionId
                        )
                );
    }


    // =========================================================
    // DELETE PERMISSION
    // =========================================================

    public void deletePermission(UUID permissionId) {

        Permission permission =
                getPermission(permissionId);

        permissionRepository.delete(permission);
    }
}