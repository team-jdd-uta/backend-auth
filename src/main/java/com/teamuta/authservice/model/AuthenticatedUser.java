package com.teamuta.authservice.model;

public record AuthenticatedUser(String userId, String username, String email) {
}
