package com.authsphere.authsphere_backend.identity.role;

import com.authsphere.authsphere_backend.identity.permission.Permission;
import com.authsphere.authsphere_backend.identity.permission.PermissionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class RoleService {

    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;

    // =========================================================
    // CREATE ROLE
    // =========================================================

    public Role createRole(
            String name,
            String description
    ) {

        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException(
                    "Role name is required"
            );
        }

        String normalizedName =
                name.trim().toUpperCase();

        if (roleRepository.existsByName(normalizedName)) {
            throw new IllegalArgumentException(
                    "Role already exists: " + normalizedName
            );
        }

        Role role = Role.builder()
                .name(normalizedName)
                .description(description)
                .build();

        return roleRepository.save(role);
    }


    // =========================================================
    // GET ALL ROLES
    // =========================================================

    @Transactional(readOnly = true)
    public List<Role> getAllRoles() {

        return roleRepository.findAll();
    }


    // =========================================================
    // GET ROLE
    // =========================================================

    @Transactional(readOnly = true)
    public Role getRole(UUID roleId) {

        return roleRepository.findById(roleId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Role not found: " + roleId
                        )
                );
    }


    // =========================================================
    // DELETE ROLE
    // =========================================================

    public void deleteRole(UUID roleId) {

        Role role = getRole(roleId);

        roleRepository.delete(role);
    }


    // =========================================================
    // ADD PERMISSION TO ROLE
    // =========================================================

    public Role addPermission(
            UUID roleId,
            UUID permissionId
    ) {

        Role role =
                getRole(roleId);

        Permission permission =
                permissionRepository.findById(permissionId)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Permission not found: "
                                                + permissionId
                                )
                        );

        role.getPermissions()
                .add(permission);

        return roleRepository.save(role);
    }


    // =========================================================
    // REMOVE PERMISSION FROM ROLE
    // =========================================================

    public Role removePermission(
            UUID roleId,
            UUID permissionId
    ) {

        Role role =
                getRole(roleId);

        role.getPermissions()
                .removeIf(permission ->
                        permission.getId()
                                .equals(permissionId)
                );

        return roleRepository.save(role);
    }
}