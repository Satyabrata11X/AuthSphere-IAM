package com.authsphere.authsphere_backend.identity.auth.dto.response;

import com.authsphere.authsphere_backend.identity.user.AccountStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileResponse {

    private UUID id;

    private String firstName;

    private String lastName;

    private String email;

    private String employeeId;

    private String phoneNumber;

    private String profileImageUrl;

    private boolean emailVerified;

    private boolean mfaEnabled;

    private AccountStatus accountStatus;

    private LocalDateTime lastLoginAt;

    /*
     * RBAC information
     */
    private List<String> roles;

    private List<String> permissions;
}