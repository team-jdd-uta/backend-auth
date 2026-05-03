package com.teamuta.authservice.service;

import com.teamuta.authservice.model.AuthDecision;
import com.teamuta.authservice.model.AuthenticatedUser;
import com.teamuta.authservice.model.VerifiedToken;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

@Service
public class AuthDecisionService {
    private final JwtVerifier jwtVerifier;
    private final UserMappingClient userMappingClient;
    private final List<RoutePolicy> publicRoutes;

    public AuthDecisionService(JwtVerifier jwtVerifier, UserMappingClient userMappingClient, List<RoutePolicy> publicRoutes) {
        this.jwtVerifier = jwtVerifier;
        this.userMappingClient = userMappingClient;
        this.publicRoutes = publicRoutes;
    }

    public AuthDecision authorize(String method, String path, Map<String, String> headers) {
        if (isPublic(method, path)) {
            return AuthDecision.allow(Map.of());
        }

        String bearerToken = resolveBearerToken(headers);
        if (bearerToken == null) {
            return AuthDecision.deny(401, "missing bearer token");
        }

        VerifiedToken token;
        try {
            token = jwtVerifier.verify(bearerToken);
        } catch (JwtException | IllegalArgumentException exception) {
            return AuthDecision.deny(401, "invalid bearer token");
        }

        Optional<AuthenticatedUser> mappedUser = userMappingClient.findByCognitoSub(token.subject());
        if (mappedUser.isEmpty()) {
            return AuthDecision.deny(403, "user mapping not found");
        }

        AuthenticatedUser user = mappedUser.get();
        return AuthDecision.allow(Map.of(
                "X-Auth-Gateway", "team9",
                "X-Cognito-Sub", token.subject(),
                "X-User-Id", user.userId(),
                "X-User-Name", firstNonBlank(user.username(), token.username(), user.userId()),
                "X-User-Email", firstNonBlank(user.email(), token.email(), "")
        ));
    }

    private boolean isPublic(String method, String path) {
        return publicRoutes.stream().anyMatch(route -> route.matches(method, path));
    }

    private String resolveBearerToken(Map<String, String> headers) {
        String authorization = header(headers, "authorization");
        if (authorization == null || !authorization.toLowerCase(Locale.ROOT).startsWith("bearer ")) {
            return null;
        }
        String token = authorization.substring("bearer ".length()).trim();
        return token.isBlank() ? null : token;
    }

    private String header(Map<String, String> headers, String key) {
        if (headers == null || headers.isEmpty()) {
            return null;
        }
        for (Map.Entry<String, String> entry : headers.entrySet()) {
            if (entry.getKey() != null && entry.getKey().equalsIgnoreCase(key)) {
                return entry.getValue();
            }
        }
        return null;
    }

    private String firstNonBlank(String... values) {
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value;
            }
        }
        return "";
    }
}
