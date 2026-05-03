package com.teamuta.authservice.model;

import java.util.Map;

public record AuthDecision(boolean allowed, int status, String message, Map<String, String> responseHeaders) {
    public static AuthDecision allow(Map<String, String> responseHeaders) {
        return new AuthDecision(true, 200, "allowed", responseHeaders);
    }

    public static AuthDecision deny(int status, String message) {
        return new AuthDecision(false, status, message, Map.of());
    }
}
