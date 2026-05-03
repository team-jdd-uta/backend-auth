package com.teamuta.authservice.service;

import java.util.Locale;

public record RoutePolicy(String method, String pathPrefix) {
    public RoutePolicy {
        method = method == null ? "*" : method.toUpperCase(Locale.ROOT);
        pathPrefix = normalize(pathPrefix);
    }

    public boolean matches(String requestMethod, String requestPath) {
        String normalizedMethod = requestMethod == null ? "" : requestMethod.toUpperCase(Locale.ROOT);
        String normalizedPath = normalize(requestPath);
        return ("*".equals(method) || method.equals(normalizedMethod))
                && (normalizedPath.equals(pathPrefix) || normalizedPath.startsWith(pathPrefix + "/"));
    }

    public static RoutePolicy parse(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("route policy must not be blank");
        }
        String[] parts = value.trim().split("\\s+", 2);
        if (parts.length == 1) {
            return new RoutePolicy("*", parts[0]);
        }
        return new RoutePolicy(parts[0], parts[1]);
    }

    private static String normalize(String path) {
        if (path == null || path.isBlank()) {
            return "/";
        }
        String withoutQuery = path.split("\\?", 2)[0].trim();
        return withoutQuery.startsWith("/") ? withoutQuery : "/" + withoutQuery;
    }
}
