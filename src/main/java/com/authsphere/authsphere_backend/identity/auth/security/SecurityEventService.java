package com.authsphere.authsphere_backend.identity.auth.security;

import com.authsphere.authsphere_backend.identity.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class SecurityEventService {

    private final SecurityEventRepository securityEventRepository;

    public void recordEvent(
            User user,
            SecurityEventType eventType,
            boolean success,
            String ipAddress,
            String details
    ) {

        SecurityEvent securityEvent =
                SecurityEvent.builder()
                        .user(user)
                        .eventType(eventType)
                        .success(success)
                        .ipAddress(ipAddress)
                        .occurredAt(LocalDateTime.now())
                        .details(details)
                        .build();

        securityEventRepository.save(securityEvent);
    }


    public void recordAdminEvent(
            User actor,
            User target,
            SecurityEventType eventType,
            String ipAddress,
            String details
    ) {

        SecurityEvent securityEvent =
                SecurityEvent.builder()
                        .user(target)
                        .actorId(actor.getId())
                        .targetType("USER")
                        .targetId(target.getId())
                        .eventType(eventType)
                        .success(true)
                        .ipAddress(ipAddress)
                        .occurredAt(LocalDateTime.now())
                        .details(details)
                        .build();

        securityEventRepository.save(securityEvent);
    }
}