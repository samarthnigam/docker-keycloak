package com.example.demo.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * User controller - requires authentication.
 * Demonstrates endpoints accessible to any authenticated user.
 */
@RestController
public class UserController {

    /**
     * Protected endpoint accessible to any authenticated user.
     * Shows basic user information from the authentication context.
     */
    @GetMapping("/user")
    public ResponseEntity<Map<String, Object>> getUserInfo(Authentication authentication) {
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Welcome! You are authenticated.");
        response.put("username", authentication.getName());
        response.put("authorities", authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList()));
        response.put("timestamp", System.currentTimeMillis());

        return ResponseEntity.ok(response);
    }
}