package com.authsphere.authsphere_backend.identity.superadmin.service;

import com.authsphere.authsphere_backend.identity.auth.security.SecurityEventRepository;
import com.authsphere.authsphere_backend.identity.session.SessionRepository;
import com.authsphere.authsphere_backend.identity.session.SessionStatus;
import com.authsphere.authsphere_backend.identity.superadmin.dto.SuperAdminDashboardResponse;
import com.authsphere.authsphere_backend.identity.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SuperAdminDashboardService {

    private final UserRepository userRepository;
    private final SessionRepository sessionRepository;
    private final SecurityEventRepository securityEventRepository;

    public SuperAdminDashboardResponse getDashboard() {

        return SuperAdminDashboardResponse.builder()
                .totalUsers(userRepository.count())
                .totalAdministrators(
                        userRepository.countAdministrators()
                )
                .activeSessions(
                        sessionRepository.countByStatus(
                                SessionStatus.ACTIVE
                        )
                )
                .mfaEnabledUsers(
                        userRepository.countByMfaEnabledTrue()
                )
                .securityEvents(
                        securityEventRepository.count()
                )
                .build();
    }
}