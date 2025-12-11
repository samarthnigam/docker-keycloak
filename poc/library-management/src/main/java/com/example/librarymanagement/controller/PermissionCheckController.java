package com.example.librarymanagement.controller;

import com.example.librarymanagement.service.RolePermissionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/permission-check")
public class PermissionCheckController {

    @Autowired
    private RolePermissionService rolePermissionService;

    @PostMapping("/has-permission")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String, Object>> checkPermission(
            @RequestBody Map<String, Object> request,
            Authentication authentication) {

        String permission = (String) request.get("permission");
        String resourceType = (String) request.get("resourceType");
        Long resourceId = request.get("resourceId") != null ? Long.valueOf(request.get("resourceId").toString()) : null;
        Map<String, Object> context = (Map<String, Object>) request.get("context");

        // Extract user ID from JWT token
        Jwt jwt = (Jwt) authentication.getPrincipal();
        String userId = extractUserIdFromToken(jwt);

        boolean hasPermission = rolePermissionService.hasPermission(userId, permission, resourceType, resourceId, context);

        Map<String, Object> response = Map.of(
            "hasPermission", hasPermission,
            "userId", userId,
            "permission", permission,
            "resourceType", resourceType,
            "resourceId", resourceId
        );

        return ResponseEntity.ok(response);
    }

    @GetMapping("/user-permissions")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String, Object>> getUserPermissions(Authentication authentication) {
        Jwt jwt = (Jwt) authentication.getPrincipal();
        String userId = extractUserIdFromToken(jwt);

        var permissions = rolePermissionService.getUserPermissions(userId);
        var roles = rolePermissionService.getUserRoles(userId);

        Map<String, Object> response = new java.util.HashMap<>();
        response.put("userId", userId);
        response.put("permissions", permissions != null ? permissions : java.util.Collections.emptySet());
        response.put("roles", roles != null ? roles : java.util.Collections.emptySet());

        return ResponseEntity.ok(response);
    }

    @GetMapping("/user-roles")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String, Object>> getUserRoles(Authentication authentication) {
        Jwt jwt = (Jwt) authentication.getPrincipal();
        String userId = extractUserIdFromToken(jwt);

        var roles = rolePermissionService.getUserRoles(userId);

        Map<String, Object> response = Map.of(
            "userId", userId,
            "roles", roles
        );

        return ResponseEntity.ok(response);
    }

    private String extractUserIdFromToken(Jwt jwt) {
        // Extract user ID from JWT token - adjust based on your token structure
        String subject = jwt.getSubject();
        if (subject != null && !subject.isEmpty()) {
            return subject;
        }
        
        // Try to get from preferred_username claim (Keycloak specific)
        Object preferredUsername = jwt.getClaims().get("preferred_username");
        if (preferredUsername instanceof String) {
            return (String) preferredUsername;
        }
        
        // Try to get from sub claim
        Object sub = jwt.getClaims().get("sub");
        if (sub instanceof String) {
            return (String) sub;
        }
        
        // Fallback to a default user ID for testing
        return "admin";
    }
}