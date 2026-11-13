
package com.authsphere.authsphere_backend.identity.superadmin.controller;

import com.authsphere.authsphere_backend.core.common.PageResponse;
import com.authsphere.authsphere_backend.identity.superadmin.dto.user.SuperAdminUserDetailsResponse;
import com.authsphere.authsphere_backend.identity.superadmin.dto.user.SuperAdminUserResponse;
import com.authsphere.authsphere_backend.identity.superadmin.service.SuperAdminUserService;
import com.authsphere.authsphere_backend.identity.user.User;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import com.authsphere.authsphere_backend.identity.user.AccountStatus;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/superadmin/users")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class SuperAdminUserController {

    private final SuperAdminUserService superAdminUserService;


    // =========================================================
    // GET ALL USERS WITH SEARCH, FILTERING AND PAGINATION
    // =========================================================

    @GetMapping
    public ResponseEntity<PageResponse<SuperAdminUserResponse>> getUsers(

            @RequestParam(required = false)
            String search,

            @RequestParam(required = false)
            AccountStatus accountStatus,

            @RequestParam(required = false)
            String roleName,

            @RequestParam(defaultValue = "0")
            int page,

            @RequestParam(defaultValue = "10")
            int size

    ) {

        return ResponseEntity.ok(
                superAdminUserService.getUsers(
                        search,
                        accountStatus,
                        roleName,
                        page,
                        size
                )
        );
    }


    // =========================================================
    // GET INDIVIDUAL USER DETAILS
    // =========================================================

    @GetMapping("/{userId}")
    public ResponseEntity<SuperAdminUserDetailsResponse> getUserDetails(
            @PathVariable UUID userId
    ) {

        return ResponseEntity.ok(
                superAdminUserService.getUserDetails(userId)
        );
    }


    // =========================================================
// SUSPEND USER
// =========================================================

    @PatchMapping("/{userId}/suspend")
    public ResponseEntity<Void> suspendUser(
            @PathVariable UUID userId,
            @AuthenticationPrincipal User superAdmin
    ) {

        superAdminUserService.suspendUser(
                userId,
                superAdmin.getId()
        );

        return ResponseEntity.noContent().build();
    }


// =========================================================
// ACTIVATE USER
// =========================================================

    @PatchMapping("/{userId}/activate")
    public ResponseEntity<Void> activateUser(
            @PathVariable UUID userId,
            @AuthenticationPrincipal User superAdmin
    ) {

        superAdminUserService.activateUser(
                userId,
                superAdmin.getId()
        );

        return ResponseEntity.noContent().build();
    }


// =========================================================
// SOFT DELETE USER
// =========================================================

    @DeleteMapping("/{userId}")
    public ResponseEntity<Void> deleteUser(
            @PathVariable UUID userId,
            @AuthenticationPrincipal User superAdmin
    ) {

        superAdminUserService.deleteUser(
                userId,
                superAdmin.getId()
        );

        return ResponseEntity.noContent().build();
    }
}
