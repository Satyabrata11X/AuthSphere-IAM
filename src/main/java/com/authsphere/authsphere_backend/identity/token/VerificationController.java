package com.authsphere.authsphere_backend.identity.token;

import com.authsphere.authsphere_backend.core.common.ApiResponse;
import com.authsphere.authsphere_backend.core.common.ApiStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class VerificationController {

    private final VerificationService verificationService;

    @GetMapping("/verify")
    public ResponseEntity<ApiResponse<Void>> verifyEmail(
            @RequestParam String token
    ) {

        verificationService.verifyToken(token);

        ApiResponse<Void> response = ApiResponse.<Void>builder()
                .success(true)
                .status(ApiStatus.SUCCESS)
                .message("Email verified successfully.")
                .build();

        return ResponseEntity.ok(response);
    }
}