
package com.authsphere.authsphere_backend.identity.systemhealth.dto;

public record ComponentHealthResponse(

        String name,

        String status,

        String message,

        long responseTimeMs

) {}
