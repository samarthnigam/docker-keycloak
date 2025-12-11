package com.example.demo.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * Token Info Controller - demonstrates ID Token and Access Token exploration.
 *
 * This controller shows the difference between:
 * - ID Token: Contains user identity information (sub, name, email, etc.)
 * - Access Token: Contains authorization information (scopes, roles, permissions)
 *
 * In OAuth2/OIDC flow:
 * - ID Token is for the client application (contains user info)
 * - Access Token is for the resource server (contains authorization data)
 */
@RestController
public class TokenInfoController {

    /**
     * Endpoint that returns decoded JWT token information.
     * Shows both ID token claims and access token details.
     */
    @GetMapping("/token-info")
    public ResponseEntity<Map<String, Object>> getTokenInfo(Authentication authentication) {
        Map<String, Object> response = new HashMap<>();

        if (authentication.getPrincipal() instanceof Jwt jwt) {
            // Extract all claims from the JWT (this is the Access Token in OAuth2 Resource Server context)
            Map<String, Object> tokenClaims = jwt.getClaims();

            response.put("message", "JWT Token Information");
            response.put("username", authentication.getName());
            response.put("authorities", authentication.getAuthorities());

            // Full token claims
            response.put("tokenClaims", tokenClaims);

            // Key identity claims (typically from ID Token)
            response.put("identityClaims", Map.of(
                "sub", tokenClaims.get("sub"),           // Subject (user ID)
                "preferred_username", tokenClaims.get("preferred_username"),
                "name", tokenClaims.get("name"),
                "given_name", tokenClaims.get("given_name"),
                "family_name", tokenClaims.get("family_name"),
                "email", tokenClaims.get("email"),
                "email_verified", tokenClaims.get("email_verified")
            ));

            // Key authorization claims (typically from Access Token)
            response.put("authorizationClaims", Map.of(
                "realm_access", tokenClaims.get("realm_access"),  // Keycloak realm roles
                "resource_access", tokenClaims.get("resource_access"), // Client-specific roles
                "scope", tokenClaims.get("scope"),               // OAuth2 scopes
                "iss", tokenClaims.get("iss"),                   // Issuer
                "aud", tokenClaims.get("aud"),                   // Audience
                "exp", tokenClaims.get("exp"),                   // Expiration time
                "iat", tokenClaims.get("iat"),                   // Issued at time
                "jti", tokenClaims.get("jti")                    // JWT ID
            ));

            // Token metadata
            response.put("tokenMetadata", Map.of(
                "tokenValue", jwt.getTokenValue().substring(0, 50) + "...", // First 50 chars
                "issuedAt", jwt.getIssuedAt(),
                "expiresAt", jwt.getExpiresAt(),
                "issuer", jwt.getIssuer(),
                "subject", jwt.getSubject(),
                "audience", jwt.getAudience(),
                "headers", jwt.getHeaders()
            ));

            // Explanation
            response.put("explanation", Map.of(
                "idTokenVsAccessToken",
                "In this OAuth2 Resource Server context, we receive the Access Token. " +
                "ID Tokens are typically consumed by the client application during the OAuth2 flow. " +
                "The Access Token contains both identity claims (from ID Token) and authorization claims.",

                "keycloakRoles",
                "Roles are stored in 'realm_access.roles' array. Our KeycloakRoleConverter " +
                "extracts these and converts them to Spring Security authorities with 'ROLE_' prefix.",

                "springSecurityIntegration",
                "Spring Security uses the JWT as the Authentication principal. " +
                "Authorities are extracted and used for authorization decisions."
            ));

        } else {
            response.put("error", "No JWT token found in authentication");
        }

        return ResponseEntity.ok(response);
    }
}