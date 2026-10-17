package com.authsphere.authsphere_backend.identity.user;

import com.authsphere.authsphere_backend.identity.company.Company;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import com.authsphere.authsphere_backend.identity.role.Role;

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
    List<User> findAdministratorsByCompany(Company company);

    @Query("""
        SELECT COUNT(DISTINCT u.id)
        FROM User u
        JOIN u.roles r
        WHERE u.company = :company
          AND r.name = 'ADMIN'
    """)
    long countAdministratorsByCompany(Company company);


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

    /*
     * Get all users managed by a particular Admin.
     */
    List<User> findByManagedBy(User admin);


    /*
     * Get all users managed by an Admin using the Admin UUID.
     */
    List<User> findByManagedById(UUID adminId);


    /*
     * Count users managed by a particular Admin.
     */
    long countByManagedBy(User admin);


    /*
     * Count users managed by an Admin using the Admin UUID.
     */
    long countByManagedById(UUID adminId);


    /*
     * Get users managed by an Admin inside a specific Company.
     *
     * This protects the Company → Admin → User hierarchy.
     */
    @Query("""
        SELECT u
        FROM User u
        WHERE u.company.id = :companyId
          AND u.managedBy.id = :adminId
    """)
    List<User> findManagedUsersByCompanyAndAdmin(
            UUID companyId,
            UUID adminId
    );


    /*
     * Count users managed by an Admin inside a specific Company.
     */
    @Query("""
        SELECT COUNT(u)
        FROM User u
        WHERE u.company.id = :companyId
          AND u.managedBy.id = :adminId
    """)
    long countManagedUsersByCompanyAndAdmin(
            UUID companyId,
            UUID adminId
    );
}