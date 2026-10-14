package com.authsphere.authsphere_backend.core.config;

import com.authsphere.authsphere_backend.identity.auth.security.JwtAuthenticationEntryPoint;
import com.authsphere.authsphere_backend.identity.auth.security.JwtAuthenticationFilter;
import jakarta.servlet.DispatcherType;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

    // =========================================================
    // PASSWORD ENCODER
    // =========================================================

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // =========================================================
    // SECURITY FILTER CHAIN
    // =========================================================

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http
    ) throws Exception {

        return http
                .csrf(csrf -> csrf.disable())

                .cors(Customizer.withDefaults())

                .exceptionHandling(exception ->
                        exception.authenticationEntryPoint(
                                jwtAuthenticationEntryPoint
                        )
                )

                .sessionManagement(session ->
                        session.sessionCreationPolicy(
                                SessionCreationPolicy.STATELESS
                        )
                )

                .authorizeHttpRequests(auth -> auth

                        // Allow Spring's error dispatch to reach
                        // the application's error handler.
                        .dispatcherTypeMatchers(
                                DispatcherType.ERROR
                        ).permitAll()

                        // =================================================
                        // SWAGGER / OPENAPI
                        // =================================================

                        .requestMatchers(
                                "/swagger-ui/**",
                                "/v3/api-docs/**"
                        ).permitAll()

                        // =================================================
                        // PUBLIC AUTHENTICATION ENDPOINTS
                        // =================================================

                        .requestMatchers(
                                "/api/auth/register",
                                "/api/auth/login",
                                "/api/auth/verify",
                                "/api/auth/resend-verification",
                                "/api/auth/forgot-password",
                                "/api/auth/reset-password",
                                "/api/auth/mfa/verify-login"
                        ).permitAll()

                        // =================================================
                        // RBAC MANAGEMENT — ADMIN
                        // =================================================

                        .requestMatchers(
                                "/api/roles/**",
                                "/api/permissions/**",
                                "/api/users/*/roles/**"
                        ).hasRole("ADMIN")

                        // =================================================
                        // SUPER ADMIN MANAGEMENT
                        // =================================================

                        .requestMatchers(
                                "/api/superadmin/**"
                        ).hasRole("SUPER_ADMIN")

                        // =================================================
                        // COMPANY-SCOPED DETAILS
                        // =================================================

                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/companies/my-company"
                        ).hasAnyRole(
                                "SUPER_ADMIN",
                                "ADMIN",
                                "USER"
                        )

                        // =================================================
                        // GLOBAL COMPANY MANAGEMENT — SUPER ADMIN ONLY
                        // =================================================

                        .requestMatchers(
                                "/api/companies/**"
                        ).hasRole("SUPER_ADMIN")

                        // =================================================
                        // EVERYTHING ELSE REQUIRES AUTHENTICATION
                        // =================================================

                        .anyRequest().authenticated()
                )

                .addFilterBefore(
                        jwtAuthenticationFilter,
                        UsernamePasswordAuthenticationFilter.class
                )

                .build();
    }

    // =========================================================
    // CORS CONFIGURATION
    // =========================================================

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {

        CorsConfiguration configuration =
                new CorsConfiguration();

        configuration.setAllowedOrigins(
                List.of(
                        "http://localhost:63342",
                        "http://localhost:63343",
                        "http://localhost:63344"
                )
        );

        configuration.setAllowedMethods(
                List.of(
                        "GET",
                        "POST",
                        "PUT",
                        "DELETE",
                        "PATCH",
                        "OPTIONS"
                )
        );

        configuration.setAllowedHeaders(
                List.of("*")
        );

        configuration.setAllowCredentials(false);

        UrlBasedCorsConfigurationSource source =
                new UrlBasedCorsConfigurationSource();

        source.registerCorsConfiguration(
                "/**",
                configuration
        );

        return source;
    }
}