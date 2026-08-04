package com.authsphere.authsphere_backend.identity.auth.dto.request;


import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder

public class RegisterRequest {

    private String firstName;

    private String lastName;

    private String email;

    private String password;
}
