
package com.authsphere.authsphere_backend.identity.user;

import com.authsphere.authsphere_backend.identity.role.Role;
import com.authsphere.authsphere_backend.identity.role.RoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class UserRoleService {

    private static final String SUPER_ADMIN_ROLE = "SUPER_ADMIN";
    private static final String COMPANY_ADMIN_ROLE = "ADMIN";

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    // =========================================================
    // ASSIGN ROLE TO USER
    // =========================================================

    public User assignRole(UUID userId, UUID roleId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "User not found: " + userId
                        )
                );

        Role role = roleRepository.findById(roleId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Role not found: " + roleId
                        )
                );

        validateGenericRoleAssignment(role);

        boolean alreadyAssigned = user.getRoles().stream()
                .anyMatch(existingRole ->
                        existingRole.getId().equals(role.getId())
                );

        if (!alreadyAssigned) {
            user.getRoles().add(role);
        }

        return userRepository.save(user);
    }

    // =========================================================
    // REMOVE ROLE FROM USER
    // =========================================================

    public User removeRole(UUID userId, UUID roleId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "User not found: " + userId
                        )
                );

        Role role = roleRepository.findById(roleId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Role not found: " + roleId
                        )
                );

        validateGenericRoleAssignment(role);

        boolean assigned = user.getRoles().stream()
                .anyMatch(existingRole ->
                        existingRole.getId().equals(role.getId())
                );

        if (!assigned) {
            throw new IllegalArgumentException(
                    "Role is not assigned to this user."
            );
        }

        user.getRoles().removeIf(
                existingRole -> existingRole.getId().equals(role.getId())
        );

        return userRepository.save(user);
    }

    // =========================================================
    // GET USER WITH ROLES
    // =========================================================

    @Transactional(readOnly = true)
    public User getUserWithRoles(UUID userId) {

        return userRepository.findById(userId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "User not found: " + userId
                        )
                );
    }

    // =========================================================
    // PROTECT PRIVILEGED ROLE ASSIGNMENTS
    // =========================================================

    private void validateGenericRoleAssignment(Role role) {

        String roleName = role.getName();

        if (SUPER_ADMIN_ROLE.equals(roleName)) {
            throw new IllegalArgumentException(
                    "SUPER_ADMIN cannot be assigned or removed "
                            + "through the generic user-role endpoint."
            );
        }

        if (COMPANY_ADMIN_ROLE.equals(roleName)) {
            throw new IllegalArgumentException(
                    "ADMIN must be assigned or removed through "
                            + "the company administration workflow."
            );
        }
    }
}
