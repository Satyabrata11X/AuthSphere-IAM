package com.authsphere.authsphere_backend.identity.company;

import com.authsphere.authsphere_backend.identity.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/companies")
@RequiredArgsConstructor
public class CompanyUserManagementController {

    private final CompanyUserManagementService companyUserManagementService;


    // =========================================================
    // ASSIGN USER TO ADMIN
    // =========================================================

    @PostMapping("/{companyId}/admins/{adminId}/users/{userId}")
    public ResponseEntity<User> assignUserToAdmin(
            @PathVariable UUID companyId,
            @PathVariable UUID adminId,
            @PathVariable UUID userId
    ) {

        User user = companyUserManagementService.assignUserToAdmin(
                companyId,
                adminId,
                userId
        );

        return ResponseEntity.ok(user);
    }


    // =========================================================
    // REMOVE USER FROM ADMIN
    // =========================================================

    @DeleteMapping("/{companyId}/admins/{adminId}/users/{userId}")
    public ResponseEntity<User> removeUserFromAdmin(
            @PathVariable UUID companyId,
            @PathVariable UUID adminId,
            @PathVariable UUID userId
    ) {

        User user = companyUserManagementService.removeUserFromAdmin(
                companyId,
                adminId,
                userId
        );

        return ResponseEntity.ok(user);
    }


    // =========================================================
    // GET USERS MANAGED BY ADMIN
    // =========================================================

    @GetMapping("/{companyId}/admins/{adminId}/users")
    public ResponseEntity<List<User>> getUsersManagedByAdmin(
            @PathVariable UUID companyId,
            @PathVariable UUID adminId
    ) {

        List<User> users =
                companyUserManagementService.getUsersManagedByAdmin(
                        companyId,
                        adminId
                );

        return ResponseEntity.ok(users);
    }


    // =========================================================
    // GET MANAGED USER COUNT
    // =========================================================

    @GetMapping("/{companyId}/admins/{adminId}/users/count")
    public ResponseEntity<Long> countUsersManagedByAdmin(
            @PathVariable UUID companyId,
            @PathVariable UUID adminId
    ) {

        long count =
                companyUserManagementService.countUsersManagedByAdmin(
                        companyId,
                        adminId
                );

        return ResponseEntity.ok(count);
    }
}