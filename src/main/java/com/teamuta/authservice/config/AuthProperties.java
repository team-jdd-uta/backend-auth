package com.teamuta.authservice.config;

import com.teamuta.authservice.service.RoutePolicy;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

@ConfigurationProperties(prefix = "auth")
public class AuthProperties {
    private List<String> publicRoutes = new ArrayList<>();

    public List<String> getPublicRoutes() {
        return publicRoutes;
    }

    public void setPublicRoutes(List<String> publicRoutes) {
        this.publicRoutes = publicRoutes;
    }

    public List<RoutePolicy> routePolicies() {
        return publicRoutes.stream().map(RoutePolicy::parse).toList();
    }
}
