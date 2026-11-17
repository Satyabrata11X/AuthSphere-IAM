
package com.authsphere.authsphere_backend.identity.role;

import com.authsphere.authsphere_backend.core.exception.ResourceNotFoundException;
import com.authsphere.authsphere_backend.identity.permission.Permission;
import com.authsphere.authsphere_backend.identity.permission.PermissionRepository;
import com.authsphere.authsphere_backend.identity.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class RoleService {

    private static final String SUPER_ADMIN = "SUPER_ADMIN";
    private static final String ADMIN = "ADMIN";
    private static final String USER = "USER";

    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;
    private final UserRepository userRepository;

    // CREATE ROLE

    public Role createRole(String name, String description) {

        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Role name is required.");
        }

        String normalizedName = name.trim().toUpperCase(Locale.ROOT);

        if (isBuiltInRole(normalizedName)) {
            throw new IllegalArgumentException(
                    "Built-in role names are reserved."
            );
        }

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

    // GET ALL ROLES

    @Transactional(readOnly = true)
    public List<Role> getAllRoles() {
        return roleRepository.findAll();
    }

    // GET ROLE

    @Transactional(readOnly = true)
    public Role getRole(UUID roleId) {

        return roleRepository.findById(roleId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Role not found: " + roleId
                        )
                );
    }

    // DELETE ROLE

    public void deleteRole(UUID roleId) {

        Role role = getRole(roleId);

        if (isBuiltInRole(role.getName())) {
            throw new IllegalArgumentException(
                    "Built-in roles cannot be deleted."
            );
        }

        if (userRepository.existsByRolesContaining(role)) {
            throw new IllegalArgumentException(
                    "Cannot delete a role assigned to users."
            );
        }

        roleRepository.delete(role);
    }

    // ADD PERMISSION

    public Role addPermission(UUID roleId, UUID permissionId) {

        Role role = getRole(roleId);

        validateCustomRole(role);

        Permission permission = permissionRepository.findById(permissionId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Permission not found: " + permissionId
                        )
                );

        if (permission.isDeleted()) {
            throw new IllegalArgumentException(
                    "Cannot assign a deleted permission."
            );
        }

        boolean alreadyAssigned = role.getPermissions().stream()
                .anyMatch(existing ->
                        existing.getId().equals(permission.getId())
                );

        if (!alreadyAssigned) {
            role.getPermissions().add(permission);
        }

        return roleRepository.save(role);
    }

    // REMOVE PERMISSION

    public Role removePermission(UUID roleId, UUID permissionId) {

        Role role = getRole(roleId);

        validateCustomRole(role);

        boolean assigned = role.getPermissions().stream()
                .anyMatch(existing ->
                        existing.getId().equals(permissionId)
                );

        if (!assigned) {
            throw new IllegalArgumentException(
                    "Permission is not assigned to this role."
            );
        }

        role.getPermissions().removeIf(
                existing -> existing.getId().equals(permissionId)
        );

        return roleRepository.save(role);
    }

    // HELPER METHODS

    private boolean isBuiltInRole(String roleName) {
        return SUPER_ADMIN.equals(roleName)
                || ADMIN.equals(roleName)
                || USER.equals(roleName);
    }

    private void validateCustomRole(Role role) {

        if (isBuiltInRole(role.getName())) {
            throw new IllegalArgumentException(
                    "Permissions of built-in roles cannot be modified."
            );
        }
    }
}
