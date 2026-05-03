package com.teamuta.authservice.model;

public record VerifiedToken(String subject, String username, String email) {
}
