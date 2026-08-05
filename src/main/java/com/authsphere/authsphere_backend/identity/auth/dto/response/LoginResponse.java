package com.authsphere.authsphere_backend.identity.auth.dto.response;


import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoginResponse {

    private String accessToken;
}
