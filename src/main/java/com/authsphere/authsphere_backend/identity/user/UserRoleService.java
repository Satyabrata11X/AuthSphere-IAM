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

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    // =========================================================
    // ASSIGN ROLE TO USER
    // =========================================================

    public User assignRole(
            UUID userId,
            UUID roleId
    ) {

        User user =
                userRepository.findById(userId)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "User not found: " + userId
                                )
                        );

        Role role =
                roleRepository.findById(roleId)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Role not found: " + roleId
                                )
                        );

        user.getRoles().add(role);

        return userRepository.save(user);
    }


    // =========================================================
    // REMOVE ROLE FROM USER
    // =========================================================

    public User removeRole(
            UUID userId,
            UUID roleId
    ) {

        User user =
                userRepository.findById(userId)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "User not found: " + userId
                                )
                        );

        user.getRoles().removeIf(
                role -> role.getId().equals(roleId)
        );

        return userRepository.save(user);
    }

    public User getUserWithRoles(UUID userId) {

        return userRepository.findById(userId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "User not found: " + userId
                        )
                );
    }
}