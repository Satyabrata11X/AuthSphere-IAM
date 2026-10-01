package com.authsphere.authsphere_backend.identity.role;

import com.authsphere.authsphere_backend.identity.permission.Permission;
import com.authsphere.authsphere_backend.identity.user.User;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Service
public class RbacAuthorityService {

    public Collection<SimpleGrantedAuthority> getAuthorities(User user) {

        List<SimpleGrantedAuthority> authorities =
                new ArrayList<>();

        if (user.getRoles() == null) {
            return authorities;
        }

        user.getRoles().forEach(role -> {

            // Role authority
            authorities.add(
                    new SimpleGrantedAuthority(
                            "ROLE_" + role.getName()
                    )
            );

            // Permission authorities
            if (role.getPermissions() != null) {

                for (Permission permission :
                        role.getPermissions()) {

                    authorities.add(
                            new SimpleGrantedAuthority(
                                    permission.getName()
                            )
                    );
                }
            }
        });

        return authorities;
    }
}