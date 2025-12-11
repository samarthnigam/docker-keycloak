package com.example.demo.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * Public controller - accessible without authentication.
 * Demonstrates endpoints that don't require any security.
 */
@RestController
public class PublicController {

    /**
     * Public endpoint accessible to everyone without authentication.
     */
    @GetMapping("/public")
    public ResponseEntity<Map<String, Object>> getPublicInfo() {
        Map<String, Object> response = new HashMap<>();
        response.put("message", "This is a public endpoint - no authentication required!");
        response.put("timestamp", System.currentTimeMillis());
        response.put("status", "success");

        return ResponseEntity.ok(response);
    }
}