
package com.authsphere.authsphere_backend.identity.superadmin.dto;

import com.authsphere.authsphere_backend.identity.auth.security.SecurityEventType;

import java.time.LocalDateTime;
import java.util.UUID;

public record RecentActivityResponse(

        UUID eventId,

        UUID userId,

        String userEmail,

        SecurityEventType eventType,

        boolean success,

        String ipAddress,

        LocalDateTime occurredAt,

        String details

) {
}
