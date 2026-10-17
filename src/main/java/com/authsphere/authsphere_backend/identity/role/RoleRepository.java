package com.authsphere.authsphere_backend.identity.role;

import com.authsphere.authsphere_backend.identity.permission.Permission;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface RoleRepository
        extends JpaRepository<Role, UUID> {

    Optional<Role> findByName(String name);

    boolean existsByName(String name);

    boolean existsByPermissionsContaining(Permission permission);
}