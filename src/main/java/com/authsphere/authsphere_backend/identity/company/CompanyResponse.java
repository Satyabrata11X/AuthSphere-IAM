package com.authsphere.authsphere_backend.identity.company;

import java.time.LocalDateTime;
import java.util.UUID;

public record CompanyResponse(

        UUID id,

        String name,

        String description,

        String website,

        String industry,

        CompanyStatus status,

        LocalDateTime createdAt,

        LocalDateTime updatedAt

) {
}