
package com.example.secureauth.security;

import com.example.secureauth.repository.RevokedTokenRepository;

import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

@Component
public class JwtRevocationValidator implements OAuth2TokenValidator<Jwt> {

    private final RevokedTokenRepository revokedTokenRepository;

    public JwtRevocationValidator(
            RevokedTokenRepository revokedTokenRepository
    ) {
        this.revokedTokenRepository = revokedTokenRepository;
    }

    @Override
    public OAuth2TokenValidatorResult validate(Jwt jwt) {

        String jti = jwt.getId();

        if (jti == null || revokedTokenRepository.existsByJti(jti)) {

            OAuth2Error error = new OAuth2Error(
                    "invalid_token",
                    "This token has been revoked or is invalid",
                    null
            );

            return OAuth2TokenValidatorResult.failure(error);
        }

        return OAuth2TokenValidatorResult.success();
    }
}
