package com.authsphere.authsphere_backend.identity.auth.dto.request;


import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor

public class LoginRequest {

    private String email;

    private String password;
}
