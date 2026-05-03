package com.teamuta.authservice.config;

import com.teamuta.authservice.service.RoutePolicy;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
@EnableConfigurationProperties(AuthProperties.class)
public class AuthServiceConfig {
    @Bean
    List<RoutePolicy> publicRoutePolicies(AuthProperties properties) {
        return properties.routePolicies();
    }
}
