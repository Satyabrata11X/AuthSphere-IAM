package com.authsphere.authsphere_backend.identity.superadmin.service;

import com.authsphere.authsphere_backend.identity.role.Role;
import com.authsphere.authsphere_backend.identity.superadmin.dto.SuperAdminUserResponse;
import com.authsphere.authsphere_backend.identity.superadmin.dto.SuperAdminUserStatusRequest;
import com.authsphere.authsphere_backend.identity.user.User;
import com.authsphere.authsphere_backend.identity.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SuperAdminUserService {

    private final UserRepository userRepository;


    // =========================================================
    // GET ALL USERS
    // =========================================================

    public List<SuperAdminUserResponse> getAllUsers() {

        return userRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }


    // =========================================================
    // GET USER BY ID
    // =========================================================

    public SuperAdminUserResponse getUserById(UUID userId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "User not found: " + userId
                        )
                );

        return mapToResponse(user);
    }


    // =========================================================
    // UPDATE USER ACCOUNT STATUS
    // =========================================================

    @Transactional
    public SuperAdminUserResponse updateUserStatus(
            UUID userId,
            SuperAdminUserStatusRequest request,
            User currentUser
    ) {

        // -----------------------------------------------------
        // PREVENT SUPER ADMIN FROM CHANGING OWN STATUS
        // -----------------------------------------------------

        if (currentUser.getId().equals(userId)) {
            throw new IllegalArgumentException(
                    "Super Admin cannot change their own account status."
            );
        }


        // -----------------------------------------------------
        // FIND TARGET USER
        // -----------------------------------------------------

        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "User not found: " + userId
                        )
                );


        // -----------------------------------------------------
        // VALIDATE REQUEST
        // -----------------------------------------------------

        if (request.getAccountStatus() == null) {
            throw new IllegalArgumentException(
                    "Account status is required."
            );
        }


        // -----------------------------------------------------
        // ALLOWED SUPER ADMIN STATUS VALUES
        // -----------------------------------------------------

        switch (request.getAccountStatus()) {

            case ACTIVE:
            case INACTIVE:
            case SUSPENDED:
            case DISABLED:

                user.setAccountStatus(
                        request.getAccountStatus()
                );

                break;


            // -------------------------------------------------
            // SYSTEM-MANAGED STATES
            // -------------------------------------------------

            case LOCKED:
            case PENDING_VERIFICATION:

                throw new IllegalArgumentException(
                        "This account status cannot be managed " +
                                "through the Super Admin status endpoint."
                );
        }


        User savedUser =
                userRepository.save(user);

        return mapToResponse(savedUser);
    }


    // =========================================================
    // MAP USER TO RESPONSE
    // =========================================================

    private SuperAdminUserResponse mapToResponse(User user) {

        List<String> roles = user.getRoles()
                .stream()
                .map(Role::getName)
                .toList();

        return SuperAdminUserResponse.builder()
                .id(user.getId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .employeeId(user.getEmployeeId())
                .phoneNumber(user.getPhoneNumber())
                .emailVerified(user.isEmailVerified())
                .mfaEnabled(user.isMfaEnabled())
                .accountStatus(user.getAccountStatus())
                .lastLoginAt(user.getLastLoginAt())
                .roles(roles)
                .build();
    }
}