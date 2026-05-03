package com.teamuta.authservice.controller;

import com.teamuta.authservice.model.AuthDecision;
import com.teamuta.authservice.service.AuthDecisionService;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.http.ResponseEntity;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AuthControllerTest {

    @Test
    void forwardsEnvoyOriginalMethodAndPathToDecisionService() {
        AuthDecisionService service = mock(AuthDecisionService.class);
        Map<String, String> headers = Map.of(
                "x-envoy-original-method", "POST",
                "x-envoy-original-path", "/api/room/rooms",
                "authorization", "Bearer abc"
        );
        when(service.authorize("POST", "/api/room/rooms", headers))
                .thenReturn(AuthDecision.allow(Map.of("X-User-Id", "user-1")));

        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/authorize");

        ResponseEntity<String> response = new AuthController(service).authorize(headers, request);

        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getHeaders().getFirst("X-User-Id")).isEqualTo("user-1");
        verify(service).authorize("POST", "/api/room/rooms", headers);
    }

    @Test
    void derivesOriginalMethodAndPathFromEnvoyPrefixedAuthorizeRequest() {
        AuthDecisionService service = mock(AuthDecisionService.class);
        Map<String, String> headers = Map.of("authorization", "Bearer abc");
        when(service.authorize("GET", "/api/user/users/info/user-1", headers))
                .thenReturn(AuthDecision.allow(Map.of("X-User-Id", "user-1")));

        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/authorize/api/user/users/info/user-1");

        ResponseEntity<String> response = new AuthController(service).authorize(headers, request);

        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getHeaders().getFirst("X-User-Id")).isEqualTo("user-1");
        verify(service).authorize("GET", "/api/user/users/info/user-1", headers);
    }
}
