package com.authsphere.authsphere_backend.identity.auth.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ResetPasswordRequest {

    @NotBlank(message = "Reset token is required.")
    private String token;

    @NotBlank(message = "New password is required.")
    private String newPassword;
}