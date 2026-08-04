package com.authsphere.authsphere_backend.identity.auth.dto.response;

import lombok.*;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder


public class RegisterResponse {


    private UUID userId;

    private String firstName;

    private String lastName;

    private String email;
}
