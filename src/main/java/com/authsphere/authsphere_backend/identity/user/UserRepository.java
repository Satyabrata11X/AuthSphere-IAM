
package com.authsphere.authsphere_backend.identity.user;

import com.authsphere.authsphere_backend.identity.company.Company;
import com.authsphere.authsphere_backend.identity.role.Role;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {

    boolean existsByEmail(String email);

    Optional<User> findByEmail(String email);

    boolean existsByRolesContaining(Role role);


    // =========================================================
    // GLOBAL USER STATISTICS
    // =========================================================

    long countByMfaEnabledTrue();

    long countByAccountStatus(AccountStatus accountStatus);


    // =========================================================
    // COMPANY USER STATISTICS
    // =========================================================

    long countByCompany(Company company);

    long countByCompanyAndAccountStatus(
            Company company,
            AccountStatus accountStatus
    );

    List<User> findByCompany(Company company);


    // =========================================================
    // COMPANY ADMINISTRATORS
    // =========================================================

    @Query("""
        SELECT u
        FROM User u
        JOIN u.roles r
        WHERE u.company = :company
          AND r.name = 'ADMIN'
    """)
    List<User> findAdministratorsByCompany(
            @Param("company") Company company
    );

    @Query("""
        SELECT COUNT(DISTINCT u.id)
        FROM User u
        JOIN u.roles r
        WHERE u.company = :company
          AND r.name = 'ADMIN'
    """)
    long countAdministratorsByCompany(
            @Param("company") Company company
    );


    // =========================================================
    // GLOBAL ADMINISTRATORS
    // =========================================================

    @Query("""
        SELECT COUNT(DISTINCT u.id)
        FROM User u
        JOIN u.roles r
        WHERE r.name = 'ADMIN'
    """)
    long countAdministrators();


    // =========================================================
    // GLOBAL COMPANY USERS
    // =========================================================

    @Query("""
        SELECT COUNT(u)
        FROM User u
        WHERE u.company IS NOT NULL
    """)
    long countCompanyUsers();


    // =========================================================
    // ADMIN → MANAGED USERS
    // =========================================================

    List<User> findByManagedBy(User admin);

    List<User> findByManagedById(UUID adminId);

    long countByManagedBy(User admin);

    long countByManagedById(UUID adminId);


    @Query("""
        SELECT u
        FROM User u
        WHERE u.company.id = :companyId
          AND u.managedBy.id = :adminId
    """)
    List<User> findManagedUsersByCompanyAndAdmin(
            @Param("companyId") UUID companyId,
            @Param("adminId") UUID adminId
    );


    @Query("""
        SELECT COUNT(u)
        FROM User u
        WHERE u.company.id = :companyId
          AND u.managedBy.id = :adminId
    """)
    long countManagedUsersByCompanyAndAdmin(
            @Param("companyId") UUID companyId,
            @Param("adminId") UUID adminId
    );


    // =========================================================
    // SUPERADMIN DASHBOARD USER STATISTICS
    // =========================================================

    @Query("""
        SELECT COUNT(u)
        FROM User u
        WHERE u.deleted = false
    """)
    long countDashboardUsers();


    @Query("""
        SELECT COUNT(u)
        FROM User u
        WHERE u.deleted = false
          AND u.accountStatus = :accountStatus
    """)
    long countDashboardUsersByAccountStatus(
            @Param("accountStatus") AccountStatus accountStatus
    );


    @Query("""
        SELECT COUNT(u)
        FROM User u
        WHERE u.deleted = false
          AND u.mfaEnabled = true
    """)
    long countDashboardMfaEnabledUsers();


    @Query("""
        SELECT COUNT(u)
        FROM User u
        WHERE u.deleted = false
          AND u.company IS NOT NULL
    """)
    long countDashboardCompanyUsers();


    @Query("""
        SELECT COUNT(DISTINCT u.id)
        FROM User u
        JOIN u.roles r
        WHERE u.deleted = false
          AND r.name = 'ADMIN'
    """)
    long countDashboardAdministrators();


    // =========================================================
    // SUPERADMIN USER GROWTH ANALYTICS
    // =========================================================

    @Query("""
        SELECT FUNCTION('DATE', u.createdAt), COUNT(u)
        FROM User u
        WHERE u.deleted = false
          AND u.createdAt >= :startDate
          AND u.createdAt < :endDate
        GROUP BY FUNCTION('DATE', u.createdAt)
        ORDER BY FUNCTION('DATE', u.createdAt)
    """)
    List<Object[]> findDashboardUserGrowth(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );


    // =========================================================
    // SUPERADMIN USERS BY ROLE ANALYTICS
    // =========================================================

    @Query("""
        SELECT r.name, COUNT(DISTINCT u.id)
        FROM User u
        JOIN u.roles r
        WHERE u.deleted = false
        GROUP BY r.name
        ORDER BY r.name
    """)
    List<Object[]> findDashboardUsersByRole();


    // =========================================================
    // SUPERADMIN LOCKED USER STATISTICS
    // =========================================================

    @Query("""
        SELECT COUNT(u)
        FROM User u
        WHERE u.deleted = false
          AND u.lockedUntil IS NOT NULL
          AND u.lockedUntil > :now
    """)
    long countDashboardCurrentlyLockedUsers(
            @Param("now") LocalDateTime now
    );


    // =========================================================
    // SUPERADMIN USER MANAGEMENT
    // =========================================================

    @Query("""
        SELECT DISTINCT u
        FROM User u
        LEFT JOIN u.roles r
        WHERE u.deleted = false
          AND (
              :search = ''
              OR LOWER(u.firstName) LIKE LOWER(CONCAT('%', :search, '%'))
              OR LOWER(u.lastName) LIKE LOWER(CONCAT('%', :search, '%'))
              OR LOWER(u.email) LIKE LOWER(CONCAT('%', :search, '%'))
              OR LOWER(u.employeeId) LIKE LOWER(CONCAT('%', :search, '%'))
          )
          AND (
              :accountStatus IS NULL
              OR u.accountStatus = :accountStatus
          )
          AND (
              :roleName IS NULL
              OR r.name = :roleName
          )
        ORDER BY u.createdAt DESC
    """)
    Page<User> searchSuperAdminUsers(
            @Param("search") String search,
            @Param("accountStatus") AccountStatus accountStatus,
            @Param("roleName") String roleName,
            Pageable pageable
    );


    // =========================================================
    // FIND ACTIVE USER DETAILS BY ID
    // =========================================================

    @Query("""
        SELECT DISTINCT u
        FROM User u
        LEFT JOIN FETCH u.roles
        LEFT JOIN FETCH u.company
        WHERE u.id = :userId
          AND u.deleted = false
    """)
    Optional<User> findActiveUserDetailsById(
            @Param("userId") UUID userId
    );

}
