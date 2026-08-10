package com.authsphere.authsphere_backend.identity.token;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class VerificationController {

    private final VerificationService verificationService;

    @GetMapping("/verify")
    public ResponseEntity<String> verifyEmail(
            @RequestParam String token
    ) {

        verificationService.verifyToken(token);

        return ResponseEntity.ok(
                "Email verified successfully."
        );
    }
}