package com.authsphere.authsphere_backend.identity.mfa;

import dev.samstevens.totp.code.CodeVerifier;
import dev.samstevens.totp.code.DefaultCodeGenerator;
import dev.samstevens.totp.code.DefaultCodeVerifier;
import dev.samstevens.totp.secret.DefaultSecretGenerator;
import dev.samstevens.totp.secret.SecretGenerator;
import dev.samstevens.totp.time.SystemTimeProvider;
import org.springframework.stereotype.Service;

@Service
public class TotpService {

    private final SecretGenerator secretGenerator;
    private final CodeVerifier codeVerifier;

    public TotpService() {

        this.secretGenerator =
                new DefaultSecretGenerator();

        this.codeVerifier =
                new DefaultCodeVerifier(
                        new DefaultCodeGenerator(),
                        new SystemTimeProvider()
                );
    }

    /**
     * Generates a new secret for MFA.
     */
    public String generateSecret() {

        return secretGenerator.generate();
    }

    /**
     * Verifies a TOTP code against the user's MFA secret.
     */
    public boolean verifyCode(
            String secret,
            String code
    ) {

        return codeVerifier.isValidCode(
                secret,
                code
        );
    }
}