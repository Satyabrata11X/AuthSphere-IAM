package com.authsphere.authsphere_backend.identity.superadmin.dto;

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
public class SuperAdminUserResponse {

    private UUID id;

    private String firstName;

    private String lastName;

    private String email;

    private String employeeId;

    private String phoneNumber;

    private boolean emailVerified;

    private boolean mfaEnabled;

    private AccountStatus accountStatus;

    private LocalDateTime lastLoginAt;

    private List<String> roles;
}