package com.authsphere.authsphere_backend.identity.permission.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Getter
@Builder
@AllArgsConstructor
public class PermissionResponse {

    private UUID id;
    private String name;
    private String description;
}