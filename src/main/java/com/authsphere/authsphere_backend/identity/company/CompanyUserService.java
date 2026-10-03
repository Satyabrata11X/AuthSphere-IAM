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
public class CompanyUserService {

    private final CompanyRepository companyRepository;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    /*
     * Add an existing user to a company.
     */
    public User addUser(
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

        /*
         * SUPER_ADMIN is a platform-level account
         * and must not become a company user.
         */
        boolean isSuperAdmin = user.getRoles()
                .stream()
                .anyMatch(role ->
                        "SUPER_ADMIN".equals(role.getName())
                );

        if (isSuperAdmin) {
            throw new IllegalArgumentException(
                    "A SUPER_ADMIN cannot be assigned to a company."
            );
        }

        /*
         * Prevent moving a user between companies
         * without explicitly removing them first.
         */
        if (user.getCompany() != null
                && !user.getCompany()
                .getId()
                .equals(companyId)) {

            throw new IllegalArgumentException(
                    "User already belongs to another company."
            );
        }

        Role userRole = roleRepository.findByName("USER")
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "USER role not found."
                        )
                );

        user.setCompany(company);

        /*
         * If the user is not an ADMIN, make sure
         * they have the normal USER role.
         */
        boolean isAdmin = user.getRoles()
                .stream()
                .anyMatch(role ->
                        "ADMIN".equals(role.getName())
                );

        if (!isAdmin) {
            user.getRoles().add(userRole);
        }

        return userRepository.save(user);
    }

    /*
     * Get all users belonging to a company.
     */
    @Transactional(readOnly = true)
    public List<User> getCompanyUsers(
            UUID companyId
    ) {

        Company company = companyRepository.findById(companyId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Company not found: " + companyId
                        )
                );

        return userRepository.findByCompany(company);
    }

    /*
     * Get one user belonging to a specific company.
     */
    @Transactional(readOnly = true)
    public User getCompanyUser(
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

        return user;
    }

    /*
     * Remove a user from a company.
     *
     * This does not delete the User record.
     */
    public User removeUser(
            UUID companyId,
            UUID userId
    ) {

        User user = getCompanyUser(companyId, userId);

        /*
         * An ADMIN should be handled through
         * CompanyAdminService instead.
         */
        boolean isAdmin = user.getRoles()
                .stream()
                .anyMatch(role ->
                        "ADMIN".equals(role.getName())
                );

        if (isAdmin) {
            throw new IllegalArgumentException(
                    "Company Admin must be removed through "
                            + "CompanyAdminService."
            );
        }

        user.setCompany(null);

        user.getRoles().removeIf(role ->
                "USER".equals(role.getName())
        );

        return userRepository.save(user);
    }
}