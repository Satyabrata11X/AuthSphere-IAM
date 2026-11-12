package com.authsphere.authsphere_backend.identity.company;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface CompanyRepository
        extends JpaRepository<Company, UUID> {

    Optional<Company> findByName(String name);

    boolean existsByName(String name);

    long countByStatus(CompanyStatus status);


    // =========================================================
// SUPERADMIN DASHBOARD COMPANY STATISTICS
// =========================================================

    @Query("""
    SELECT COUNT(c)
    FROM Company c
    WHERE c.deleted = false
""")
    long countDashboardCompanies();


    @Query("""
    SELECT COUNT(c)
    FROM Company c
    WHERE c.deleted = false
      AND c.status = :status
""")
    long countDashboardCompaniesByStatus(
            @Param("status") CompanyStatus status
    );
}