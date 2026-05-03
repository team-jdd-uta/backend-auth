package com.teamuta.authservice.service;

import com.teamuta.authservice.model.AuthDecision;
import com.teamuta.authservice.model.AuthenticatedUser;
import com.teamuta.authservice.model.VerifiedToken;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class AuthDecisionServiceTest {

    @Test
    void allowsPublicReadRoutesWithoutToken() {
        AuthDecisionService service = service(
                token -> {
                    throw new AssertionError("public route must not verify token");
                },
                sub -> Optional.empty()
        );

        assertThat(service.authorize("GET", "/api/room/rooms", Map.of()).allowed()).isTrue();
        assertThat(service.authorize("GET", "/api/room/rooms/room-1", Map.of()).allowed()).isTrue();
        assertThat(service.authorize("GET", "/api/chat-history/comments/user/user-1/room/room-1", Map.of()).allowed()).isTrue();
    }

    @Test
    void requiresTokenForRoomMutations() {
        AuthDecisionService service = service(
                token -> {
                    throw new AssertionError("missing token must not be verified");
                },
                sub -> Optional.empty()
        );

        AuthDecision decision = service.authorize("POST", "/api/room/rooms", Map.of());

        assertThat(decision.allowed()).isFalse();
        assertThat(decision.status()).isEqualTo(401);
        assertThat(decision.message()).isEqualTo("missing bearer token");
    }

    @Test
    void returnsTrustedHeadersForValidTokenAndMappedUser() {
        AuthDecisionService service = service(
                token -> new VerifiedToken("cognito-sub-1", "streamer@example.com", "streamer@example.com"),
                sub -> Optional.of(new AuthenticatedUser("user-1", "streamer@example.com", "streamer@example.com"))
        );

        AuthDecision decision = service.authorize("POST", "/api/room/rooms", Map.of(
                "authorization", "Bearer token-value",
                "x-user-id", "attacker"
        ));

        assertThat(decision.allowed()).isTrue();
        assertThat(decision.responseHeaders()).containsEntry("X-Auth-Gateway", "team9");
        assertThat(decision.responseHeaders()).containsEntry("X-Cognito-Sub", "cognito-sub-1");
        assertThat(decision.responseHeaders()).containsEntry("X-User-Id", "user-1");
        assertThat(decision.responseHeaders()).containsEntry("X-User-Email", "streamer@example.com");
    }

    @Test
    void rejectsWhenCognitoUserCannotBeMappedToInternalUser() {
        AuthDecisionService service = service(
                token -> new VerifiedToken("unknown-sub", "new@example.com", "new@example.com"),
                sub -> Optional.empty()
        );

        AuthDecision decision = service.authorize("GET", "/api/user/users/info/user-1", Map.of(
                "authorization", "Bearer token-value"
        ));

        assertThat(decision.allowed()).isFalse();
        assertThat(decision.status()).isEqualTo(403);
        assertThat(decision.message()).isEqualTo("user mapping not found");
    }

    private AuthDecisionService service(JwtVerifier verifier, UserMappingClient mappingClient) {
        return new AuthDecisionService(
                verifier,
                mappingClient,
                List.of(
                        new RoutePolicy("GET", "/health"),
                        new RoutePolicy("GET", "/api/room/rooms"),
                        new RoutePolicy("GET", "/api/chat-history"),
                        new RoutePolicy("POST", "/auth/login"),
                        new RoutePolicy("POST", "/auth/register"),
                        new RoutePolicy("POST", "/auth/refresh")
                )
        );
    }
}
