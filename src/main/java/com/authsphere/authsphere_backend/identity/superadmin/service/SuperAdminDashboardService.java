
package com.authsphere.authsphere_backend.identity.superadmin.service;

import com.authsphere.authsphere_backend.identity.auth.security.LoginAttemptRepository;
import com.authsphere.authsphere_backend.identity.auth.security.SecurityEventRepository;
import com.authsphere.authsphere_backend.identity.company.CompanyRepository;
import com.authsphere.authsphere_backend.identity.company.CompanyStatus;
import com.authsphere.authsphere_backend.identity.session.SessionRepository;
import com.authsphere.authsphere_backend.identity.session.SessionStatus;
import com.authsphere.authsphere_backend.identity.superadmin.dto.SuperAdminDashboardResponse;
import com.authsphere.authsphere_backend.identity.user.AccountStatus;
import com.authsphere.authsphere_backend.identity.user.UserRepository;
import com.authsphere.authsphere_backend.identity.superadmin.dto.UserGrowthResponse;
import com.authsphere.authsphere_backend.identity.superadmin.dto.UserRoleDistributionResponse;
import com.authsphere.authsphere_backend.identity.superadmin.dto.AuthenticationActivityResponse;

import com.authsphere.authsphere_backend.core.common.PageResponse;
import com.authsphere.authsphere_backend.identity.superadmin.dto.RecentActivityResponse;
import com.authsphere.authsphere_backend.identity.auth.security.SecurityEvent;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import java.util.List;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;


import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SuperAdminDashboardService {

    private final CompanyRepository companyRepository;
    private final UserRepository userRepository;
    private final SessionRepository sessionRepository;
    private final SecurityEventRepository securityEventRepository;
    private final LoginAttemptRepository loginAttemptRepository;


    public SuperAdminDashboardResponse getDashboard() {

        // Company statistics
        long totalCompanies =
                companyRepository.countDashboardCompanies();

        long activeCompanies =
                companyRepository.countDashboardCompaniesByStatus(
                        CompanyStatus.ACTIVE
                );

        // User statistics
        long totalUsers =
                userRepository.countDashboardUsers();

        long companyUsers =
                userRepository.countDashboardCompanyUsers();

        long totalAdministrators =
                userRepository.countDashboardAdministrators();

        long activeUsers =
                userRepository.countDashboardUsersByAccountStatus(
                        AccountStatus.ACTIVE
                );

        long lockedUsers =
                userRepository.countDashboardUsersByAccountStatus(
                        AccountStatus.LOCKED
                );

        long suspendedUsers =
                userRepository.countDashboardUsersByAccountStatus(
                        AccountStatus.SUSPENDED
                );

        long mfaEnabledUsers =
                userRepository.countDashboardMfaEnabledUsers();

        // Active session statistics
        long activeSessions =
                sessionRepository.countValidActiveSessions(
                        SessionStatus.ACTIVE,
                        LocalDateTime.now()
                );

        // Security statistics
        long totalSecurityEvents =
                securityEventRepository.count();

        return new SuperAdminDashboardResponse(
                totalCompanies,
                activeCompanies,
                totalUsers,
                companyUsers,
                totalAdministrators,
                activeUsers,
                lockedUsers,
                suspendedUsers,
                mfaEnabledUsers,
                activeSessions,
                totalSecurityEvents
        );
    }

    public List<UserGrowthResponse> getUserGrowth(int days) {

        if (days < 1 || days > 90) {
            throw new IllegalArgumentException(
                    "Days must be between 1 and 90"
            );
        }

        LocalDate today = LocalDate.now();
        LocalDate startDate = today.minusDays(days - 1L);

        LocalDateTime startDateTime =
                startDate.atStartOfDay();

        LocalDateTime endDateTime =
                today.plusDays(1).atStartOfDay();

        List<Object[]> results =
                userRepository.findDashboardUserGrowth(
                        startDateTime,
                        endDateTime
                );

        Map<LocalDate, Long> registrationsByDate =
                results.stream().collect(Collectors.toMap(
                        row -> ((java.sql.Date) row[0]).toLocalDate(),
                        row -> ((Number) row[1]).longValue()
                ));

        List<UserGrowthResponse> response = new ArrayList<>();

        for (int i = 0; i < days; i++) {

            LocalDate date = startDate.plusDays(i);

            response.add(new UserGrowthResponse(
                    date,
                    registrationsByDate.getOrDefault(date, 0L)
            ));
        }

        return response;
    }

    public List<UserRoleDistributionResponse> getUsersByRole() {

        List<Object[]> results =
                userRepository.findDashboardUsersByRole();

        long totalUsers = results.stream()
                .mapToLong(row -> ((Number) row[1]).longValue())
                .sum();

        List<UserRoleDistributionResponse> response =
                new ArrayList<>();

        for (Object[] row : results) {

            String roleName = (String) row[0];

            long userCount = ((Number) row[1]).longValue();

            double percentage = totalUsers == 0
                    ? 0.0
                    : Math.round((userCount * 10000.0) / totalUsers) / 100.0;

            response.add(new UserRoleDistributionResponse(
                    roleName,
                    userCount,
                    percentage
            ));
        }

        return response;
    }

    public List<AuthenticationActivityResponse> getAuthenticationActivity(int days) {

        if (days < 1 || days > 90) {
            throw new IllegalArgumentException(
                    "Days must be between 1 and 90"
            );
        }

        LocalDate today = LocalDate.now();
        LocalDate startDate = today.minusDays(days - 1L);

        LocalDateTime startDateTime =
                startDate.atStartOfDay();

        LocalDateTime endDateTime =
                today.plusDays(1).atStartOfDay();

        List<Object[]> results =
                loginAttemptRepository.findDashboardAuthenticationActivity(
                        startDateTime,
                        endDateTime
                );

        Map<LocalDate, long[]> activityByDate = new HashMap<>();

        // Initialize every date with zero successful and failed attempts
        for (int i = 0; i < days; i++) {
            activityByDate.put(
                    startDate.plusDays(i),
                    new long[]{0L, 0L}
            );
        }

        // Populate actual database results
        for (Object[] row : results) {

            LocalDate date =
                    ((java.sql.Date) row[0]).toLocalDate();

            boolean success =
                    (Boolean) row[1];

            long count =
                    ((Number) row[2]).longValue();

            long[] counts = activityByDate.get(date);

            if (counts != null) {
                if (success) {
                    counts[0] = count;
                } else {
                    counts[1] = count;
                }
            }
        }

        List<AuthenticationActivityResponse> response =
                new ArrayList<>();

        for (int i = 0; i < days; i++) {

            LocalDate date = startDate.plusDays(i);

            long[] counts = activityByDate.get(date);

            response.add(new AuthenticationActivityResponse(
                    date,
                    counts[0],
                    counts[1]
            ));
        }

        return response;
    }

    public PageResponse<RecentActivityResponse> getRecentActivity(
            int page,
            int size
    ) {
        if (page < 0) {
            throw new IllegalArgumentException("Page number cannot be negative");
        }

        if (size < 1 || size > 100) {
            throw new IllegalArgumentException("Page size must be between 1 and 100");
        }

        Pageable pageable = PageRequest.of(page, size);

        Page<SecurityEvent> activityPage =
                securityEventRepository.findDashboardRecentActivity(pageable);

        List<RecentActivityResponse> content = activityPage.getContent()
                .stream()
                .map(event -> new RecentActivityResponse(
                        event.getId(),
                        event.getUser().getId(),
                        event.getUser().getEmail(),
                        event.getEventType(),
                        event.isSuccess(),
                        event.getIpAddress(),
                        event.getOccurredAt(),
                        event.getDetails()
                ))
                .toList();

        return PageResponse.<RecentActivityResponse>builder()
                .content(content)
                .page(activityPage.getNumber())
                .size(activityPage.getSize())
                .totalElements(activityPage.getTotalElements())
                .totalPages(activityPage.getTotalPages())
                .first(activityPage.isFirst())
                .last(activityPage.isLast())
                .build();
    }
}
