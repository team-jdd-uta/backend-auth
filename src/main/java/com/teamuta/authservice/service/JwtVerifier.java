package com.teamuta.authservice.service;

import com.teamuta.authservice.model.VerifiedToken;

public interface JwtVerifier {
    VerifiedToken verify(String bearerToken);
}
