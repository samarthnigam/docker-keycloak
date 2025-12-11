package com.example.librarymanagement.config;

import com.example.librarymanagement.service.RolePermissionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class KeycloakJwtAuthenticationConverter implements Converter<Jwt, AbstractAuthenticationToken> {

    @Autowired
    private RolePermissionService rolePermissionService;

    @Override
    public AbstractAuthenticationToken convert(Jwt jwt) {
        System.out.println("Converting JWT to AuthenticationToken");
        Collection<GrantedAuthority> authorities = extractAuthorities(jwt);
        System.out.println("Extracted authorities: " + authorities);
        return new JwtAuthenticationToken(jwt, authorities);
    }

    private Collection<GrantedAuthority> extractAuthorities(Jwt jwt) {
        List<GrantedAuthority> authorities = new ArrayList<>();

        // Extract Keycloak realm roles
        Map<String, Object> realmAccess = jwt.getClaim("realm_access");
        if (realmAccess != null) {
            @SuppressWarnings("unchecked")
            List<String> roles = (List<String>) realmAccess.get("roles");
            if (roles != null) {
                authorities.addAll(roles.stream()
                        .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                        .collect(Collectors.toList()));
            }
        }

        // Extract custom roles from database
        try {
            String userId = extractUserIdFromToken(jwt);
            if (userId != null) {
                List<String> customRoles = rolePermissionService.getUserRoleNames(userId);
                authorities.addAll(customRoles.stream()
                        .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                        .collect(Collectors.toList()));
            }
        } catch (Exception e) {
            // Log error but don't fail authentication
            System.err.println("Error loading custom roles: " + e.getMessage());
        }

        return authorities;
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
        return "db3b9afd-68ec-41c3-9065-128230ace759";
    }
}