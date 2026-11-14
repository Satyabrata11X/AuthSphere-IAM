package com.authsphere.authsphere_backend.identity.company;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

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


    // =========================================================
// SUPERADMIN COMPANY MANAGEMENT
// =========================================================

    @Query(
            value = """
        SELECT c
        FROM Company c
        WHERE c.deleted = false
          AND (
              :search = ''
              OR LOWER(c.name) LIKE LOWER(CONCAT('%', :search, '%'))
              OR LOWER(c.industry) LIKE LOWER(CONCAT('%', :search, '%'))
          )
          AND (
              :status IS NULL
              OR c.status = :status
          )
        ORDER BY c.createdAt DESC
        """,
            countQuery = """
        SELECT COUNT(c)
        FROM Company c
        WHERE c.deleted = false
          AND (
              :search = ''
              OR LOWER(c.name) LIKE LOWER(CONCAT('%', :search, '%'))
              OR LOWER(c.industry) LIKE LOWER(CONCAT('%', :search, '%'))
          )
          AND (
              :status IS NULL
              OR c.status = :status
          )
        """
    )
    Page<Company> searchSuperAdminCompanies(
            @Param("search") String search,
            @Param("status") CompanyStatus status,
            Pageable pageable
    );
}