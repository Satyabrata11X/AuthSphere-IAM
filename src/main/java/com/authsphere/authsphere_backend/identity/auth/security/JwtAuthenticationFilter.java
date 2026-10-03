package com.authsphere.authsphere_backend.identity.auth.security;

import com.authsphere.authsphere_backend.identity.auth.service.JwtService;
import com.authsphere.authsphere_backend.identity.role.RbacAuthorityService;
import com.authsphere.authsphere_backend.identity.session.Session;
import com.authsphere.authsphere_backend.identity.session.SessionService;
import com.authsphere.authsphere_backend.identity.user.User;
import com.authsphere.authsphere_backend.identity.user.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;

    private final UserRepository userRepository;

    private final SessionService sessionService;

    private final RbacAuthorityService rbacAuthorityService;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");

        // =====================================================
        // NO JWT
        // =====================================================

        if (authHeader == null
                || !authHeader.startsWith("Bearer ")) {

            filterChain.doFilter(request, response);
            return;
        }

        // =====================================================
        // EXTRACT JWT
        // =====================================================

        String jwt = authHeader.substring(7);

        try {

            // =================================================
            // EXTRACT USERNAME FROM JWT
            // =================================================

            String username = jwtService.extractUsername(jwt);

            if (username != null
                    && SecurityContextHolder
                    .getContext()
                    .getAuthentication() == null) {

                // =============================================
                // FIND USER
                // =============================================

                Optional<User> optionalUser =
                        userRepository.findByEmail(username);

                if (optionalUser.isPresent()) {

                    User user = optionalUser.get();

                    // =========================================
                    // VALIDATE JWT
                    // =========================================

                    if (jwtService.isTokenValid(jwt, user)) {

                        // =====================================
                        // EXTRACT SESSION
                        // =====================================

                        UUID sessionId =
                                jwtService.extractSessionId(jwt);

                        Session session =
                                sessionService.getSession(sessionId);

                        // =====================================
                        // VALIDATE SESSION
                        // =====================================

                        if (session != null
                                && sessionService
                                .isSessionActive(session)) {

                            // =================================
                            // UPDATE SESSION ACTIVITY
                            // =================================

                            sessionService
                                    .updateLastActivity(session);

                            // =================================
                            // LOAD RBAC AUTHORITIES
                            // =================================

                            Collection<SimpleGrantedAuthority>
                                    authorities =
                                    rbacAuthorityService
                                            .getAuthorities(user);

                            // =================================
                            // CREATE AUTHENTICATION
                            // =================================

                            UsernamePasswordAuthenticationToken
                                    authenticationToken =
                                    new UsernamePasswordAuthenticationToken(
                                            user,
                                            null,
                                            authorities
                                    );

                            // =================================
                            // STORE AUTHENTICATION
                            // =================================

                            SecurityContextHolder
                                    .getContext()
                                    .setAuthentication(
                                            authenticationToken
                                    );
                        }
                    }
                }
            }

        } catch (Exception e) {

            // =================================================
            // TEMPORARY DEBUGGING
            // =================================================

            System.err.println(
                    "========== JWT AUTHENTICATION ERROR =========="
            );

            System.err.println(
                    "Request: "
                            + request.getMethod()
                            + " "
                            + request.getRequestURI()
            );

            e.printStackTrace();

            System.err.println(
                    "=============================================="
            );
        }

        // =====================================================
        // CONTINUE FILTER CHAIN
        // =====================================================

        filterChain.doFilter(request, response);
    }
}