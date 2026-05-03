package com.teamuta.authservice.service;

import com.teamuta.authservice.model.VerifiedToken;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtDecoders;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.stereotype.Component;

@Component
public class CognitoJwtVerifier implements JwtVerifier {
    private final JwtDecoder jwtDecoder;
    private final String clientId;

    public CognitoJwtVerifier(@Value("${cognito.issuer-uri}") String issuerUri,
                              @Value("${cognito.user-pool-id}") String userPoolId,
                              @Value("${cognito.region}") String region,
                              @Value("${cognito.client-id}") String clientId) {
        String resolvedIssuerUri = issuerUri;
        if (resolvedIssuerUri == null || resolvedIssuerUri.isBlank()) {
            if (userPoolId == null || userPoolId.isBlank()) {
                throw new IllegalStateException("cognito.issuer-uri or cognito.user-pool-id must be configured");
            }
            String resolvedRegion = region == null || region.isBlank() ? "ap-northeast-2" : region;
            resolvedIssuerUri = "https://cognito-idp." + resolvedRegion + ".amazonaws.com/" + userPoolId;
        }
        this.jwtDecoder = JwtDecoders.fromIssuerLocation(resolvedIssuerUri);
        this.clientId = clientId;
    }

    @Override
    public VerifiedToken verify(String bearerToken) {
        Jwt jwt = jwtDecoder.decode(bearerToken);
        verifyAudience(jwt);
        verifyTokenUse(jwt);
        return new VerifiedToken(
                jwt.getSubject(),
                claim(jwt, "cognito:username", claim(jwt, "username", "")),
                claim(jwt, "email", "")
        );
    }

    private void verifyAudience(Jwt jwt) {
        if (clientId == null || clientId.isBlank()) {
            return;
        }
        String clientClaim = claim(jwt, "client_id", "");
        if (clientId.equals(clientClaim) || jwt.getAudience().contains(clientId)) {
            return;
        }
        throw new JwtException("unexpected audience");
    }

    private void verifyTokenUse(Jwt jwt) {
        String tokenUse = claim(jwt, "token_use", "");
        if ("access".equals(tokenUse) || "id".equals(tokenUse)) {
            return;
        }
        throw new JwtException("unexpected token_use");
    }

    private String claim(Jwt jwt, String name, String fallback) {
        Object value = jwt.getClaims().get(name);
        return value == null ? fallback : String.valueOf(value);
    }
}
