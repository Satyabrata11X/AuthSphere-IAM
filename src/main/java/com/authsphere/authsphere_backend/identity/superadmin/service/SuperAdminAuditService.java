package com.authsphere.authsphere_backend.identity.superadmin.service;

import com.authsphere.authsphere_backend.core.common.PageResponse;
import com.authsphere.authsphere_backend.identity.auth.security.SecurityEvent;
import com.authsphere.authsphere_backend.identity.auth.security.SecurityEventRepository;
import com.authsphere.authsphere_backend.identity.auth.security.SecurityEventType;
import com.authsphere.authsphere_backend.identity.superadmin.dto.AuditLogResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SuperAdminAuditService {

    private final SecurityEventRepository securityEventRepository;

    public PageResponse<AuditLogResponse> getAuditLogs(
            int page,
            int size,
            SecurityEventType eventType,
            Boolean success,
            LocalDateTime startDate,
            LocalDateTime endDate
    ) {

        if (page < 0) {
            throw new IllegalArgumentException("Page number cannot be negative");
        }

        if (size < 1 || size > 100) {
            throw new IllegalArgumentException("Page size must be between 1 and 100");
        }

        if (startDate != null && endDate != null && startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("Start date cannot be after end date");
        }

        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by(Sort.Direction.DESC, "occurredAt")
        );

        Page<SecurityEvent> events = securityEventRepository.findAuditLogs(
                eventType,
                success,
                startDate,
                endDate,
                pageable
        );

        Page<AuditLogResponse> responsePage = events.map(event ->
                new AuditLogResponse(
                        event.getId(),
                        event.getActorId(),
                        event.getTargetId(),
                        event.getTargetType(),
                        event.getUser() != null ? event.getUser().getEmail() : null,
                        event.getEventType(),
                        event.isSuccess(),
                        event.getIpAddress(),
                        event.getOccurredAt(),
                        event.getDetails()
                )
        );

        return PageResponse.<AuditLogResponse>builder()
                .content(responsePage.getContent())
                .page(responsePage.getNumber())
                .size(responsePage.getSize())
                .totalElements(responsePage.getTotalElements())
                .totalPages(responsePage.getTotalPages())
                .first(responsePage.isFirst())
                .last(responsePage.isLast())
                .build();
    }
}