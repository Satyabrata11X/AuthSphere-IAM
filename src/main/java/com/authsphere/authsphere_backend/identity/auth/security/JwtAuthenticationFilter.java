
package com.authsphere.authsphere_backend.identity.auth.security;

import com.authsphere.authsphere_backend.identity.auth.service.JwtService;
import com.authsphere.authsphere_backend.identity.role.RbacAuthorityService;
import com.authsphere.authsphere_backend.identity.session.Session;
import com.authsphere.authsphere_backend.identity.session.SessionService;
import com.authsphere.authsphere_backend.identity.user.AccountStatus;
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

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String jwt = authHeader.substring(7);

        try {

            // =================================================
            // EXTRACT USERNAME
            // =================================================

            String username = jwtService.extractUsername(jwt);

            if (username == null ||
                    SecurityContextHolder.getContext().getAuthentication() != null) {

                filterChain.doFilter(request, response);
                return;
            }

            // =================================================
            // FIND USER
            // =================================================

            Optional<User> optionalUser =
                    userRepository.findByEmail(username);

            if (optionalUser.isEmpty()) {
                SecurityContextHolder.clearContext();
                filterChain.doFilter(request, response);
                return;
            }

            User user = optionalUser.get();

            // =================================================
            // VALIDATE ACCOUNT STATUS
            // =================================================

            if (user.isDeleted()
                    || user.getAccountStatus() != AccountStatus.ACTIVE) {

                SecurityContextHolder.clearContext();
                filterChain.doFilter(request, response);
                return;
            }

            // =================================================
            // VALIDATE JWT
            // =================================================

            if (!jwtService.isTokenValid(jwt, user)) {
                SecurityContextHolder.clearContext();
                filterChain.doFilter(request, response);
                return;
            }

            // =================================================
            // EXTRACT SESSION ID
            // =================================================

            UUID sessionId = jwtService.extractSessionId(jwt);

            if (sessionId == null) {
                SecurityContextHolder.clearContext();
                filterChain.doFilter(request, response);
                return;
            }

            // =================================================
            // FIND SESSION
            // =================================================

            Session session = sessionService.getSession(sessionId);

            // =================================================
            // VALIDATE SESSION
            // =================================================

            if (session == null || !sessionService.isSessionActive(session)) {
                SecurityContextHolder.clearContext();
                filterChain.doFilter(request, response);
                return;
            }

            // =================================================
            // UPDATE SESSION ACTIVITY
            // =================================================

            sessionService.updateLastActivity(session);

            // =================================================
            // LOAD RBAC AUTHORITIES
            // =================================================

            Collection<SimpleGrantedAuthority> authorities =
                    rbacAuthorityService.getAuthorities(user);

            // =================================================
            // CREATE AUTHENTICATION
            // =================================================

            UsernamePasswordAuthenticationToken authenticationToken =
                    new UsernamePasswordAuthenticationToken(
                            user,
                            null,
                            authorities
                    );

            // =================================================
            // SET SECURITY CONTEXT
            // =================================================

            SecurityContextHolder.getContext()
                    .setAuthentication(authenticationToken);

        } catch (Exception e) {

            // Reject authentication if JWT processing fails.
            SecurityContextHolder.clearContext();

        }

        // =====================================================
        // CONTINUE FILTER CHAIN
        // =====================================================

        filterChain.doFilter(request, response);
    }
}
