package com.authsphere.authsphere_backend.identity.user.mapper;

import com.authsphere.authsphere_backend.identity.user.User;
import com.authsphere.authsphere_backend.identity.user.dto.UserRoleResponse;
import org.springframework.stereotype.Component;

@Component
public class UserRoleMapper {

    public UserRoleResponse toResponse(User user) {

        return new UserRoleResponse(
                user.getId(),
                user.getFirstName(),
                user.getLastName(),
                user.getEmail(),
                user.getEmployeeId(),
                user.getAccountStatus(),
                user.getRoles()
                        .stream()
                        .map(role -> role.getName())
                        .sorted()
                        .toList()
        );
    }
}