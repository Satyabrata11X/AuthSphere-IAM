package com.authsphere.authsphere_backend.identity.user.dto;

import com.authsphere.authsphere_backend.identity.user.AccountStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder
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
}