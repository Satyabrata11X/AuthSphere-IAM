package com.authsphere.authsphere_backend.identity.company;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class CompanyService {

    private final CompanyRepository companyRepository;

    /*
     * Create a new company.
     */
    public Company createCompany(
            String name,
            String description,
            String website,
            String industry
    ) {

        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException(
                    "Company name is required."
            );
        }

        String normalizedName = name.trim();

        if (companyRepository.existsByName(normalizedName)) {
            throw new IllegalArgumentException(
                    "Company already exists: " + normalizedName
            );
        }

        Company company = Company.builder()
                .name(normalizedName)
                .description(description)
                .website(website)
                .industry(industry)
                .status(CompanyStatus.ACTIVE)
                .build();

        return companyRepository.save(company);
    }

    /*
     * Get all companies.
     */
    @Transactional(readOnly = true)
    public List<Company> getAllCompanies() {
        return companyRepository.findAll();
    }

    /*
     * Get company by ID.
     */
    @Transactional(readOnly = true)
    public Company getCompany(UUID companyId) {

        return companyRepository.findById(companyId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Company not found: " + companyId
                        )
                );
    }

    /*
     * Update company information.
     */
    public Company updateCompany(
            UUID companyId,
            String name,
            String description,
            String website,
            String industry
    ) {

        Company company = getCompany(companyId);

        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException(
                    "Company name is required."
            );
        }

        String normalizedName = name.trim();

        companyRepository.findByName(normalizedName)
                .filter(existing ->
                        !existing.getId().equals(companyId)
                )
                .ifPresent(existing -> {
                    throw new IllegalArgumentException(
                            "Company already exists: "
                                    + normalizedName
                    );
                });

        company.setName(normalizedName);
        company.setDescription(description);
        company.setWebsite(website);
        company.setIndustry(industry);

        return companyRepository.save(company);
    }

    /*
     * Change company status.
     */
    public Company updateCompanyStatus(
            UUID companyId,
            CompanyStatus status
    ) {

        if (status == null) {
            throw new IllegalArgumentException(
                    "Company status is required."
            );
        }

        Company company = getCompany(companyId);

        company.setStatus(status);

        return companyRepository.save(company);
    }

    /*
     * Delete a company.
     */
    public void deleteCompany(UUID companyId) {

        Company company = getCompany(companyId);

        companyRepository.delete(company);
    }
}