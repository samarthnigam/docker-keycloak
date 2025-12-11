package com.example.demo.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Admin controller - requires ADMIN role.
 * Demonstrates role-based access control.
 */
@RestController
public class AdminController {

    /**
     * Admin-only endpoint requiring ADMIN role.
     * Uses @PreAuthorize annotation for method-level security.
     */
    @GetMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getAdminInfo(Authentication authentication) {
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Welcome to the admin area! You have ADMIN privileges.");
        response.put("username", authentication.getName());
        response.put("authorities", authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList()));
        response.put("adminAccess", true);
        response.put("timestamp", System.currentTimeMillis());

        return ResponseEntity.ok(response);
    }
}