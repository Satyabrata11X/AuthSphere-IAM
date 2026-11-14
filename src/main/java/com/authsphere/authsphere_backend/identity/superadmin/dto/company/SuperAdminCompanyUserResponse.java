package com.authsphere.authsphere_backend.identity.superadmin.dto.company;

import com.authsphere.authsphere_backend.identity.user.AccountStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder
public class SuperAdminCompanyUserResponse {

    private UUID userId;
    private String firstName;
    private String lastName;
    private String email;
    private String employeeId;

    private AccountStatus accountStatus;

    private boolean emailVerified;
    private boolean mfaEnabled;

    private LocalDateTime createdAt;
}