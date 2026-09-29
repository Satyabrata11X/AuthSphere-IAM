package com.authsphere.authsphere_backend.identity.role.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Getter
@Builder
@AllArgsConstructor
public class RoleResponse {

    private UUID id;
    private String name;
    private String description;
}