package com.authsphere.authsphere_backend.identity.auth.controller;


import com.authsphere.authsphere_backend.identity.auth.dto.request.RegisterRequest;
import com.authsphere.authsphere_backend.identity.auth.dto.request.LoginRequest;
import com.authsphere.authsphere_backend.identity.auth.dto.response.LoginResponse;
import com.authsphere.authsphere_backend.identity.auth.dto.response.RegisterResponse;
import com.authsphere.authsphere_backend.identity.auth.service.AuthenticationService;
import com.authsphere.authsphere_backend.core.common.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor

public class AuthenticationController {

    private final AuthenticationService authenticationService;

    @PostMapping("/register")
    public ApiResponse<RegisterResponse> register(
            @RequestBody RegisterRequest request) {

        return authenticationService.register(request);
    }

    @PostMapping("/login")
    public ApiResponse<LoginResponse> login(
            @RequestBody LoginRequest request) {

        return authenticationService.login(request);
    }
}
