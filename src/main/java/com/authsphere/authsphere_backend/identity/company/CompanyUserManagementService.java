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
public class CompanyUserManagementService {

    private final CompanyRepository companyRepository;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;


    // =========================================================
    // ASSIGN USER TO ADMIN
    // =========================================================

    public User assignUserToAdmin(
            UUID companyId,
            UUID adminId,
            UUID userId
    ) {

        Company company = companyRepository.findById(companyId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Company not found: " + companyId
                        )
                );

        User admin = userRepository.findById(adminId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Admin not found: " + adminId
                        )
                );

        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "User not found: " + userId
                        )
                );


        // -----------------------------------------------------
        // ADMIN VALIDATION
        // -----------------------------------------------------

        boolean isAdmin = admin.getRoles()
                .stream()
                .anyMatch(role ->
                        "ADMIN".equals(role.getName())
                );

        if (!isAdmin) {
            throw new IllegalArgumentException(
                    "Selected user is not a company ADMIN."
            );
        }


        // -----------------------------------------------------
        // ADMIN CANNOT BE SUPER ADMIN
        // -----------------------------------------------------

        boolean isSuperAdmin = admin.getRoles()
                .stream()
                .anyMatch(role ->
                        "SUPER_ADMIN".equals(role.getName())
                );

        if (isSuperAdmin) {
            throw new IllegalArgumentException(
                    "SUPER_ADMIN cannot manage company users."
            );
        }


        // -----------------------------------------------------
        // ADMIN CANNOT MANAGE THEMSELVES
        // -----------------------------------------------------

        if (admin.getId().equals(user.getId())) {
            throw new IllegalArgumentException(
                    "An Admin cannot manage themselves."
            );
        }


        // -----------------------------------------------------
        // ADMIN MUST BELONG TO THE COMPANY
        // -----------------------------------------------------

        if (admin.getCompany() == null ||
                !admin.getCompany().getId().equals(company.getId())) {

            throw new IllegalArgumentException(
                    "Admin does not belong to this company."
            );
        }


        // -----------------------------------------------------
        // USER MUST BELONG TO THE COMPANY
        // -----------------------------------------------------

        if (user.getCompany() == null ||
                !user.getCompany().getId().equals(company.getId())) {

            throw new IllegalArgumentException(
                    "User does not belong to this company."
            );
        }


        // -----------------------------------------------------
        // USER CANNOT BE SUPER ADMIN
        // -----------------------------------------------------

        boolean userIsSuperAdmin = user.getRoles()
                .stream()
                .anyMatch(role ->
                        "SUPER_ADMIN".equals(role.getName())
                );

        if (userIsSuperAdmin) {
            throw new IllegalArgumentException(
                    "SUPER_ADMIN cannot be assigned to an Admin."
            );
        }


        // -----------------------------------------------------
        // USER MUST HAVE USER ROLE
        // -----------------------------------------------------

        Role userRole = roleRepository.findByName("USER")
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "USER role not found."
                        )
                );

        boolean hasUserRole = user.getRoles()
                .stream()
                .anyMatch(role ->
                        "USER".equals(role.getName())
                );

        if (!hasUserRole) {
            user.getRoles().add(userRole);
        }


        // -----------------------------------------------------
        // ASSIGN ADMIN AS MANAGER
        // -----------------------------------------------------

        user.setManagedBy(admin);

        return userRepository.save(user);
    }


    // =========================================================
    // REMOVE USER FROM ADMIN
    // =========================================================

    public User removeUserFromAdmin(
            UUID companyId,
            UUID adminId,
            UUID userId
    ) {

        Company company = companyRepository.findById(companyId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Company not found: " + companyId
                        )
                );

        User admin = userRepository.findById(adminId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Admin not found: " + adminId
                        )
                );

        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "User not found: " + userId
                        )
                );


        // -----------------------------------------------------
        // ADMIN VALIDATION
        // -----------------------------------------------------

        boolean isAdmin = admin.getRoles()
                .stream()
                .anyMatch(role ->
                        "ADMIN".equals(role.getName())
                );

        if (!isAdmin) {
            throw new IllegalArgumentException(
                    "Selected user is not a company ADMIN."
            );
        }


        // -----------------------------------------------------
        // COMPANY VALIDATION
        // -----------------------------------------------------

        if (admin.getCompany() == null ||
                !admin.getCompany().getId().equals(company.getId())) {

            throw new IllegalArgumentException(
                    "Admin does not belong to this company."
            );
        }

        if (user.getCompany() == null ||
                !user.getCompany().getId().equals(company.getId())) {

            throw new IllegalArgumentException(
                    "User does not belong to this company."
            );
        }


        // -----------------------------------------------------
        // MANAGER VALIDATION
        // -----------------------------------------------------

        if (user.getManagedBy() == null ||
                !user.getManagedBy().getId().equals(admin.getId())) {

            throw new IllegalArgumentException(
                    "User is not managed by this Admin."
            );
        }


        // -----------------------------------------------------
        // REMOVE MANAGER
        // -----------------------------------------------------

        user.setManagedBy(null);

        return userRepository.save(user);
    }


    // =========================================================
    // GET USERS MANAGED BY ADMIN
    // =========================================================

    @Transactional(readOnly = true)
    public List<User> getUsersManagedByAdmin(
            UUID companyId,
            UUID adminId
    ) {

        Company company = companyRepository.findById(companyId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Company not found: " + companyId
                        )
                );

        User admin = userRepository.findById(adminId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Admin not found: " + adminId
                        )
                );


        boolean isAdmin = admin.getRoles()
                .stream()
                .anyMatch(role ->
                        "ADMIN".equals(role.getName())
                );

        if (!isAdmin) {
            throw new IllegalArgumentException(
                    "Selected user is not a company ADMIN."
            );
        }


        if (admin.getCompany() == null ||
                !admin.getCompany().getId().equals(company.getId())) {

            throw new IllegalArgumentException(
                    "Admin does not belong to this company."
            );
        }


        return userRepository.findManagedUsersByCompanyAndAdmin(
                companyId,
                adminId
        );
    }


    // =========================================================
    // GET MANAGED USER COUNT
    // =========================================================

    @Transactional(readOnly = true)
    public long countUsersManagedByAdmin(
            UUID companyId,
            UUID adminId
    ) {

        // Validate that the company and admin relationship is valid.
        getUsersManagedByAdmin(companyId, adminId);

        return userRepository.countManagedUsersByCompanyAndAdmin(
                companyId,
                adminId
        );
    }
}