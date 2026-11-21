package com.authsphere.authsphere_backend.identity.auth.security;

import com.authsphere.authsphere_backend.core.common.BaseEntity;
import com.authsphere.authsphere_backend.identity.user.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "security_events")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SecurityEvent extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SecurityEventType eventType;

    @Column(nullable = false)
    private boolean success;

    @Column(nullable = false)
    private String ipAddress;

    @Column(nullable = false)
    private LocalDateTime occurredAt;

    @Column(length = 1000)
    private String details;

    // Administrator who performed the action
    @Column(name = "actor_id")
    private UUID actorId;

    // Type of affected resource
    @Column(name = "target_type", length = 50)
    private String targetType;

    // ID of affected resource
    @Column(name = "target_id")
    private UUID targetId;
}