package com.authsphere.authsphere_backend.identity.auth.service;

import com.authsphere.authsphere_backend.core.common.ApiResponse;
import com.authsphere.authsphere_backend.identity.auth.dto.request.RegisterRequest;
import com.authsphere.authsphere_backend.identity.auth.dto.response.RegisterResponse;

public interface AuthenticationService {
    ApiResponse<RegisterResponse> register(RegisterRequest request);

}
