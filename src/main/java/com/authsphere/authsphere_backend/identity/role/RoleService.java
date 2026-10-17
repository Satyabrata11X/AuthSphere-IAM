package com.authsphere.authsphere_backend.identity.role;

import com.authsphere.authsphere_backend.identity.permission.Permission;
import com.authsphere.authsphere_backend.identity.permission.PermissionRepository;
import com.authsphere.authsphere_backend.identity.user.UserRepository;
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
    private final UserRepository userRepository;

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

        if ("SUPER_ADMIN".equals(role.getName())) {
            throw new IllegalArgumentException(
                    "The SUPER_ADMIN role cannot be deleted."
            );
        }

        if ("ADMIN".equals(role.getName())) {
            throw new IllegalArgumentException(
                    "The built-in ADMIN role cannot be deleted."
            );
        }

        if ("USER".equals(role.getName())) {
            throw new IllegalArgumentException(
                    "The built-in USER role cannot be deleted."
            );
        }

        if (userRepository.existsByRolesContaining(role)) {
            throw new IllegalArgumentException(
                    "Cannot delete a role that is assigned to users."
            );
        }

        roleRepository.delete(role);
    }




    public Role addPermission(UUID roleId, UUID permissionId) {

        Role role = getRole(roleId);

        Permission permission = permissionRepository.findById(permissionId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Permission not found: " + permissionId
                        )
                );

        boolean alreadyAssigned = role.getPermissions().stream()
                .anyMatch(existing ->
                        existing.getId().equals(permission.getId())
                );

        if (!alreadyAssigned) {
            role.getPermissions().add(permission);
        }

        return roleRepository.save(role);
    }

    public Role removePermission(UUID roleId, UUID permissionId) {

        Role role = getRole(roleId);

        Permission permission = permissionRepository.findById(permissionId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Permission not found: " + permissionId
                        )
                );

        boolean assigned = role.getPermissions().stream()
                .anyMatch(existing ->
                        existing.getId().equals(permission.getId())
                );

        if (!assigned) {
            throw new IllegalArgumentException(
                    "Permission is not assigned to this role."
            );
        }

        role.getPermissions().removeIf(
                existing -> existing.getId().equals(permission.getId())
        );

        return roleRepository.save(role);
    }

}