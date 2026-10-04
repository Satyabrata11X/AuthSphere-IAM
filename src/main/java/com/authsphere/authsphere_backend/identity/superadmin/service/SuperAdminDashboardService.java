package com.authsphere.authsphere_backend.identity.superadmin.service;
import com.authsphere.authsphere_backend.identity.auth.security.SecurityEventRepository;
import com.authsphere.authsphere_backend.identity.company.CompanyRepository;
import com.authsphere.authsphere_backend.identity.company.CompanyStatus;
import com.authsphere.authsphere_backend.identity.session.SessionRepository;
import com.authsphere.authsphere_backend.identity.session.SessionStatus;
import com.authsphere.authsphere_backend.identity.superadmin.dto.SuperAdminDashboardResponse;
import com.authsphere.authsphere_backend.identity.user.AccountStatus;
import com.authsphere.authsphere_backend.identity.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SuperAdminDashboardService {

    private final CompanyRepository companyRepository;
    private final UserRepository userRepository;
    private final SessionRepository sessionRepository;
    private final SecurityEventRepository securityEventRepository;

    public SuperAdminDashboardResponse getDashboard() {

        long totalCompanies =
                companyRepository.count();

        long activeCompanies =
                companyRepository.countByStatus(CompanyStatus.ACTIVE);

        long totalUsers =
                userRepository.count();

        long companyUsers =
                userRepository.countCompanyUsers();

        long totalAdministrators =
                userRepository.countAdministrators();

        long activeUsers =
                userRepository.countByAccountStatus(AccountStatus.ACTIVE);

        long lockedUsers =
                userRepository.countByAccountStatus(AccountStatus.LOCKED);

        long suspendedUsers =
                userRepository.countByAccountStatus(AccountStatus.SUSPENDED);

        long mfaEnabledUsers =
                userRepository.countByMfaEnabledTrue();

        long activeSessions =
                sessionRepository.countByStatus(SessionStatus.ACTIVE);

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
}