package com.teamuta.authservice.service;

import com.teamuta.authservice.model.AuthenticatedUser;

import java.util.Optional;

public interface UserMappingClient {
    Optional<AuthenticatedUser> findByCognitoSub(String cognitoSub);
}
