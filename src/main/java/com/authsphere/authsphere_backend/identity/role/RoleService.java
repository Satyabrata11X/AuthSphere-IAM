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

    public Role createRole(String name, String description) {

        if (roleRepository.existsByName(name)) {
            throw new IllegalArgumentException(
                    "Role already exists: " + name
            );
        }

        Role role = Role.builder()
                .name(name)
                .description(description)
                .build();

        return roleRepository.save(role);
    }

    @Transactional(readOnly = true)
    public List<Role> getAllRoles() {
        return roleRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Role getRoleById(UUID id) {
        return roleRepository.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Role not found: " + id
                        )
                );
    }

    public void deleteRole(UUID id) {

        Role role = getRoleById(id);

        roleRepository.delete(role);
    }

    public Role assignPermission(UUID roleId, UUID permissionId) {

        Role role = getRoleById(roleId);

        Permission permission = permissionRepository.findById(permissionId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Permission not found: " + permissionId
                        )
                );

        role.getPermissions().add(permission);

        return roleRepository.save(role);
    }
}