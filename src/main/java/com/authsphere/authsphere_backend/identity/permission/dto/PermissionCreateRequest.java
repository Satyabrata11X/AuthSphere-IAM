package com.authsphere.authsphere_backend.identity.permission.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class PermissionCreateRequest {

    @NotBlank(message = "Permission name is required.")
    @Size(max = 100, message = "Permission name must not exceed 100 characters.")
    private String name;

    @Size(max = 255, message = "Description must not exceed 255 characters.")
    private String description;
}