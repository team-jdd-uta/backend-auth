package com.teamuta.authservice.controller;

import com.teamuta.authservice.model.AuthDecision;
import com.teamuta.authservice.service.AuthDecisionService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class AuthController {
    private final AuthDecisionService authDecisionService;

    public AuthController(AuthDecisionService authDecisionService) {
        this.authDecisionService = authDecisionService;
    }

    @RequestMapping("/authorize")
    public ResponseEntity<String> authorize(@RequestHeader Map<String, String> headers) {
        String method = firstHeader(headers, "x-envoy-original-method", "x-original-method", ":method");
        String path = firstHeader(headers, "x-envoy-original-path", "x-original-uri", ":path");
        AuthDecision decision = authDecisionService.authorize(method, path, headers);
        ResponseEntity.BodyBuilder response = ResponseEntity.status(HttpStatus.valueOf(decision.status()));
        decision.responseHeaders().forEach(response::header);
        return response.body(decision.message());
    }

    @RequestMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("ok");
    }

    private String firstHeader(Map<String, String> headers, String... names) {
        for (String name : names) {
            for (Map.Entry<String, String> entry : headers.entrySet()) {
                if (entry.getKey() != null && entry.getKey().equalsIgnoreCase(name)) {
                    return entry.getValue();
                }
            }
        }
        return "";
    }
}
