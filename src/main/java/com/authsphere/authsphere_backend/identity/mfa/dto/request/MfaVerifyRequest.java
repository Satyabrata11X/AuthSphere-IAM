package com.authsphere.authsphere_backend.identity.mfa.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MfaVerifyRequest {

    @NotBlank(message = "MFA verification code is required.")
    @Pattern(
            regexp = "\\d{6}",
            message = "MFA verification code must contain exactly 6 digits."
    )
    private String code;
}