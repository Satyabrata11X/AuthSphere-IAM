package com.authsphere.authsphere_backend.identity.company;

import com.authsphere.authsphere_backend.identity.role.Role;
import com.authsphere.authsphere_backend.identity.role.RoleRepository;
import com.authsphere.authsphere_backend.identity.user.User;
import com.authsphere.authsphere_backend.identity.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class CompanyAdminService {

    private final CompanyRepository companyRepository;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    /*
     * Assign an existing user as an administrator
     * of a company.
     */
    public User assignAdmin(
            UUID companyId,
            UUID userId
    ) {

        Company company = companyRepository.findById(companyId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Company not found: " + companyId
                        )
                );

        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "User not found: " + userId
                        )
                );

        Role adminRole = roleRepository.findByName("ADMIN")
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "ADMIN role not found."
                        )
                );

        /*
         * A Super Admin is a platform-level account.
         * It must not be converted into a company admin.
         */
        boolean isSuperAdmin = user.getRoles()
                .stream()
                .anyMatch(role ->
                        "SUPER_ADMIN".equals(role.getName())
                );

        if (isSuperAdmin) {
            throw new IllegalArgumentException(
                    "A SUPER_ADMIN cannot be assigned as a company ADMIN."
            );
        }

        /*
         * A User belongs to one Company because User has
         * a single company relationship.
         */
        if (user.getCompany() != null
                && !user.getCompany()
                .getId()
                .equals(companyId)) {

            throw new IllegalArgumentException(
                    "User already belongs to another company."
            );
        }

        user.setCompany(company);
        user.getRoles().add(adminRole);

        return userRepository.save(user);
    }

    /*
     * Remove the ADMIN role from a company user.
     *
     * The user remains associated with the company.
     */
    public User removeAdmin(
            UUID companyId,
            UUID userId
    ) {

        Company company = companyRepository.findById(companyId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Company not found: " + companyId
                        )
                );

        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "User not found: " + userId
                        )
                );

        if (user.getCompany() == null
                || !user.getCompany()
                .getId()
                .equals(company.getId())) {

            throw new IllegalArgumentException(
                    "User does not belong to this company."
            );
        }

        user.getRoles().removeIf(role ->
                "ADMIN".equals(role.getName())
        );

        return userRepository.save(user);
    }

    /*
     * Get all administrators belonging to a company.
     */
    @Transactional(readOnly = true)
    public List<User> getCompanyAdmins(
            UUID companyId
    ) {

        Company company = companyRepository.findById(companyId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Company not found: " + companyId
                        )
                );

        return userRepository.findAdministratorsByCompany(company);
    }
}