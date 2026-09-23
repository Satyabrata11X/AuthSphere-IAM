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

    public Permission createPermission(String name, String description) {

        if (permissionRepository.existsByName(name)) {
            throw new IllegalArgumentException(
                    "Permission already exists: " + name
            );
        }

        Permission permission = Permission.builder()
                .name(name)
                .description(description)
                .build();

        return permissionRepository.save(permission);
    }

    @Transactional(readOnly = true)
    public List<Permission> getAllPermissions() {
        return permissionRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Permission getPermissionById(UUID id) {
        return permissionRepository.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Permission not found: " + id
                        )
                );
    }

    public void deletePermission(UUID id) {

        Permission permission = getPermissionById(id);

        permissionRepository.delete(permission);
    }
}