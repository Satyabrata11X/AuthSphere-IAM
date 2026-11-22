package com.authsphere.authsphere_backend.identity.superadmin.dto;
import com.authsphere.authsphere_backend.identity.auth.security.SecurityEventType;

import java.time.LocalDateTime;
import java.util.UUID;

public record AuditLogResponse(

        UUID eventId,

        UUID actorId,

        UUID targetId,

        String targetType,

        String targetEmail,

        SecurityEventType eventType,

        boolean success,

        String ipAddress,

        LocalDateTime occurredAt,

        String details

) {
}
