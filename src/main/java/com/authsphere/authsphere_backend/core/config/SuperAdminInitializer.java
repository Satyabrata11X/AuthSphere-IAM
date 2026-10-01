package com.authsphere.authsphere_backend.core.config;

import com.authsphere.authsphere_backend.identity.role.Role;
import com.authsphere.authsphere_backend.identity.role.RoleRepository;
import com.authsphere.authsphere_backend.identity.user.AccountStatus;
import com.authsphere.authsphere_backend.identity.user.User;
import com.authsphere.authsphere_backend.identity.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class SuperAdminInitializer implements CommandLineRunner {

    private static final Logger log =
            LoggerFactory.getLogger(SuperAdminInitializer.class);

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${rbac.bootstrap.super-admin-email:}")
    private String superAdminEmail;

    @Value("${rbac.bootstrap.super-admin-password:}")
    private String superAdminPassword;

    @Override
    @Transactional
    public void run(String... args) {

        // ---------------------------------------------------------
        // 1. Validate bootstrap configuration
        // ---------------------------------------------------------

        if (superAdminEmail == null || superAdminEmail.isBlank()) {

            log.info(
                    "Super Admin bootstrap skipped: " +
                            "rbac.bootstrap.super-admin-email is not configured."
            );

            return;
        }

        if (superAdminPassword == null || superAdminPassword.isBlank()) {

            log.info(
                    "Super Admin bootstrap skipped: " +
                            "rbac.bootstrap.super-admin-password is not configured."
            );

            return;
        }

        String email = superAdminEmail.trim();


        // ---------------------------------------------------------
        // 2. Find existing user
        // ---------------------------------------------------------

        User user = userRepository
                .findByEmail(email)
                .orElse(null);


        // ---------------------------------------------------------
        // 3. Create or update bootstrap user
        // ---------------------------------------------------------

        if (user == null) {

            log.info(
                    "Creating initial Super Admin account for: {}",
                    email
            );

            user = User.builder()
                    .firstName("AuthSphere")
                    .lastName("Super Admin")
                    .email(email)
                    .passwordHash(
                            passwordEncoder.encode(
                                    superAdminPassword
                            )
                    )
                    .employeeId(
                            UUID.randomUUID().toString()
                    )
                    .emailVerified(true)
                    .mfaEnabled(false)
                    .accountStatus(AccountStatus.ACTIVE)
                    .build();

            userRepository.save(user);

            log.info(
                    "Initial Super Admin account created successfully for: {}",
                    email
            );

        } else {

            log.info(
                    "Existing bootstrap user found: {}",
                    email
            );

            // Make the designated bootstrap account active.
            user.setEmailVerified(true);

            user.setAccountStatus(
                    AccountStatus.ACTIVE
            );

            // Reset password to the configured bootstrap password.
            user.setPasswordHash(
                    passwordEncoder.encode(
                            superAdminPassword
                    )
            );

            /*
             * MFA is intentionally disabled here.
             *
             * MFA should be configured through the normal
             * MFA setup flow so that the account receives
             * a valid TOTP secret/configuration.
             */
            user.setMfaEnabled(false);

            userRepository.save(user);

            log.info(
                    "Existing bootstrap user updated successfully for: {}",
                    email
            );
        }


        // ---------------------------------------------------------
        // 4. Find SUPER_ADMIN role
        // ---------------------------------------------------------

        Role superAdminRole = roleRepository
                .findByName("SUPER_ADMIN")
                .orElse(null);

        if (superAdminRole == null) {

            log.error(
                    "Super Admin bootstrap failed: " +
                            "SUPER_ADMIN role does not exist."
            );

            return;
        }


        // ---------------------------------------------------------
        // 5. Assign SUPER_ADMIN role
        // ---------------------------------------------------------

        if (!user.getRoles().contains(superAdminRole)) {

            user.getRoles().add(superAdminRole);

            userRepository.save(user);

            log.info(
                    "SUPER_ADMIN role successfully assigned to: {}",
                    email
            );

        } else {

            log.info(
                    "SUPER_ADMIN role already assigned to: {}",
                    email
            );
        }


        // ---------------------------------------------------------
        // 6. Final confirmation
        // ---------------------------------------------------------

        log.info(
                "Super Admin bootstrap completed for: {}",
                email
        );
    }
}