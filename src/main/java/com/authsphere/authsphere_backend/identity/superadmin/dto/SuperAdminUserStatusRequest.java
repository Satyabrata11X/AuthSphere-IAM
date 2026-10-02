package com.authsphere.authsphere_backend.identity.superadmin.dto;

import com.authsphere.authsphere_backend.identity.user.AccountStatus;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SuperAdminUserStatusRequest {

    @NotNull(message = "Account status is required")
    private AccountStatus accountStatus;
}