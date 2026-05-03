package com.teamuta.authservice.controller;

import com.teamuta.authservice.model.AuthDecision;
import com.teamuta.authservice.service.AuthDecisionService;
import com.teamuta.authservice.service.JwtVerifier;
import com.teamuta.authservice.service.UserMappingClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.context.WebApplicationContext;

import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;

@SpringBootTest
class AuthControllerSecurityTest {
    @MockitoBean
    private AuthDecisionService authDecisionService;

    @MockitoBean
    private JwtVerifier jwtVerifier;

    @MockitoBean
    private UserMappingClient userMappingClient;

    @Autowired
    private WebApplicationContext context;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        this.mockMvc = webAppContextSetup(context).apply(springSecurity()).build();
    }

    @Test
    void healthIsPublic() throws Exception {
        mockMvc.perform(get("/health"))
                .andExpect(status().isOk())
                .andExpect(content().string("ok"));
    }

    @Test
    void authorizeIsPublicToEnvoyAndReturnsDecisionHeaders() throws Exception {
        when(authDecisionService.authorize(eq("GET"), eq("/api/user/me"), any()))
                .thenReturn(AuthDecision.allow(Map.of("X-User-Id", "1")));

        mockMvc.perform(post("/authorize")
                        .header("x-envoy-original-method", "GET")
                        .header("x-envoy-original-path", "/api/user/me"))
                .andExpect(status().isOk())
                .andExpect(header().string("X-User-Id", "1"))
                .andExpect(content().string("allowed"));
    }

    @Test
    void unknownEndpointIsDenied() throws Exception {
        mockMvc.perform(get("/internal"))
                .andExpect(status().isForbidden());
    }
}
