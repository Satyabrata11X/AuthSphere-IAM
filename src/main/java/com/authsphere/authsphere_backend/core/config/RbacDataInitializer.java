package com.authsphere.authsphere_backend.core.config;

import com.authsphere.authsphere_backend.identity.permission.Permission;
import com.authsphere.authsphere_backend.identity.permission.PermissionRepository;
import com.authsphere.authsphere_backend.identity.role.Role;
import com.authsphere.authsphere_backend.identity.role.RoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.List;

@Component
@RequiredArgsConstructor
public class RbacDataInitializer implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;

    @Override
    public void run(String... args) {

        // =====================================================
        // USER PERMISSIONS
        // =====================================================

        Permission userRead =
                createPermission(
                        "USER_READ",
                        "View users"
                );

        Permission userCreate =
                createPermission(
                        "USER_CREATE",
                        "Create users"
                );

        Permission userUpdate =
                createPermission(
                        "USER_UPDATE",
                        "Update users"
                );

        Permission userDelete =
                createPermission(
                        "USER_DELETE",
                        "Delete users"
                );


        // =====================================================
        // ROLE PERMISSIONS
        // =====================================================

        Permission roleRead =
                createPermission(
                        "ROLE_READ",
                        "View roles"
                );

        Permission roleCreate =
                createPermission(
                        "ROLE_CREATE",
                        "Create roles"
                );

        Permission roleUpdate =
                createPermission(
                        "ROLE_UPDATE",
                        "Update roles"
                );

        Permission roleDelete =
                createPermission(
                        "ROLE_DELETE",
                        "Delete roles"
                );


        // =====================================================
        // PERMISSION MANAGEMENT
        // =====================================================

        Permission permissionRead =
                createPermission(
                        "PERMISSION_READ",
                        "View permissions"
                );

        Permission permissionCreate =
                createPermission(
                        "PERMISSION_CREATE",
                        "Create permissions"
                );

        Permission permissionUpdate =
                createPermission(
                        "PERMISSION_UPDATE",
                        "Update permissions"
                );

        Permission permissionDelete =
                createPermission(
                        "PERMISSION_DELETE",
                        "Delete permissions"
                );


        // =====================================================
        // ADMIN MANAGEMENT
        // =====================================================

        Permission adminRead =
                createPermission(
                        "ADMIN_READ",
                        "View administrators"
                );

        Permission adminCreate =
                createPermission(
                        "ADMIN_CREATE",
                        "Create administrators"
                );

        Permission adminUpdate =
                createPermission(
                        "ADMIN_UPDATE",
                        "Update administrators"
                );

        Permission adminDelete =
                createPermission(
                        "ADMIN_DELETE",
                        "Remove administrators"
                );


        // =====================================================
        // SECURITY MANAGEMENT
        // =====================================================

        Permission securityRead =
                createPermission(
                        "SECURITY_READ",
                        "View security configuration"
                );

        Permission securityManage =
                createPermission(
                        "SECURITY_MANAGE",
                        "Manage platform security"
                );


        // =====================================================
        // PLATFORM MANAGEMENT
        // =====================================================

        Permission platformManage =
                createPermission(
                        "PLATFORM_MANAGE",
                        "Manage AuthSphere platform configuration"
                );


        // =====================================================
        // SUPER ADMIN ROLE
        // =====================================================

        Role superAdminRole =
                getOrCreateRole(
                        "SUPER_ADMIN",
                        "Full AuthSphere platform administrator"
                );

        superAdminRole.getPermissions().addAll(
                List.of(
                        userRead,
                        userCreate,
                        userUpdate,
                        userDelete,

                        roleRead,
                        roleCreate,
                        roleUpdate,
                        roleDelete,

                        permissionRead,
                        permissionCreate,
                        permissionUpdate,
                        permissionDelete,

                        adminRead,
                        adminCreate,
                        adminUpdate,
                        adminDelete,

                        securityRead,
                        securityManage,

                        platformManage
                )
        );

        roleRepository.save(superAdminRole);


        // =====================================================
        // ADMIN ROLE
        // =====================================================

        Role adminRole =
                getOrCreateRole(
                        "ADMIN",
                        "AuthSphere administrator"
                );

        adminRole.getPermissions().addAll(
                List.of(
                        userRead,
                        userCreate,
                        userUpdate,
                        userDelete,

                        roleRead,

                        permissionRead,

                        adminRead
                )
        );

        roleRepository.save(adminRole);


        // =====================================================
        // USER ROLE
        // =====================================================

        Role userRole =
                getOrCreateRole(
                        "USER",
                        "Standard AuthSphere user"
                );

        userRole.getPermissions().add(
                userRead
        );

        roleRepository.save(userRole);
    }


    // =========================================================
    // CREATE OR GET PERMISSION
    // =========================================================

    private Permission createPermission(
            String name,
            String description
    ) {

        return permissionRepository
                .findByName(name)
                .orElseGet(() ->
                        permissionRepository.save(
                                Permission.builder()
                                        .name(name)
                                        .description(description)
                                        .build()
                        )
                );
    }


    // =========================================================
    // CREATE OR GET ROLE
    // =========================================================

    private Role getOrCreateRole(
            String name,
            String description
    ) {

        return roleRepository
                .findByName(name)
                .orElseGet(() ->
                        roleRepository.save(
                                Role.builder()
                                        .name(name)
                                        .description(description)
                                        .permissions(
                                                new HashSet<>()
                                        )
                                        .build()
                        )
                );
    }
}