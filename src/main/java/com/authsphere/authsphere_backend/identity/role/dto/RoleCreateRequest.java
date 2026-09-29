package com.authsphere.authsphere_backend.identity.role.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class RoleCreateRequest {

    @NotBlank(message = "Role name is required.")
    @Size(max = 100, message = "Role name must not exceed 100 characters.")
    private String name;

    @Size(max = 255, message = "Description must not exceed 255 characters.")
    private String description;
}