package com.teamuta.authservice.service;

import com.teamuta.authservice.model.AuthenticatedUser;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

import java.util.Optional;

@Component
public class LoginUserMappingClient implements UserMappingClient {
    private final RestClient restClient;
    private final String internalToken;

    public LoginUserMappingClient(@Value("${login-service.base-url}") String baseUrl,
                                  @Value("${login-service.internal-token}") String internalToken) {
        this.restClient = RestClient.builder().baseUrl(baseUrl).build();
        this.internalToken = internalToken;
    }

    @Override
    public Optional<AuthenticatedUser> findByCognitoSub(String cognitoSub) {
        if (cognitoSub == null || cognitoSub.isBlank()) {
            return Optional.empty();
        }
        try {
            AuthenticatedUser user = restClient.get()
                    .uri("/internal/users/by-cognito-sub/{sub}", cognitoSub)
                    .header("X-Internal-Token", internalToken == null ? "" : internalToken)
                    .retrieve()
                    .body(AuthenticatedUser.class);
            return Optional.ofNullable(user);
        } catch (HttpClientErrorException.NotFound exception) {
            return Optional.empty();
        }
    }
}
