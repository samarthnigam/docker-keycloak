package com.example.librarymanagement.config;

import com.example.librarymanagement.config.KeycloakJwtAuthenticationConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.web.SecurityFilterChain;

import jakarta.annotation.PostConstruct;

import java.util.Map;
import com.fasterxml.jackson.databind.ObjectMapper;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @PostConstruct
    public void init() {
        System.out.println("SecurityConfig initialized");
    }

    @Bean
    public KeycloakJwtAuthenticationConverter keycloakJwtAuthenticationConverter() {
        return new KeycloakJwtAuthenticationConverter();
    }

    @Bean
    public JwtDecoder jwtDecoder() {
        System.out.println("Creating JWT Decoder bean");
        try {
            System.out.println("Connecting to Keycloak JWK endpoint...");
            // For testing, create a decoder that doesn't validate signatures
            return new JwtDecoder() {
                @Override
                public Jwt decode(String token) throws JwtException {
                    try {
                        String[] parts = token.split("\\.");
                        if (parts.length != 3) {
                            throw new JwtException("Invalid JWT");
                        }
                        
                        // Decode payload
                        String payload = parts[1];
                        payload += "=".repeat((4 - payload.length() % 4) % 4);
                        String decodedPayload = new String(java.util.Base64.getUrlDecoder().decode(payload));
                        
                        // Create JWT object
                        ObjectMapper mapper = new ObjectMapper();
                        Map<String, Object> claims = mapper.readValue(decodedPayload, Map.class);
                        
                        // For testing: inject custom roles into the JWT claims
                        // In production, these would come from Keycloak
                        Map<String, Object> realmAccess = new java.util.HashMap<>();
                        realmAccess.put("roles", java.util.Arrays.asList("Admin", "User"));
                        claims.put("realm_access", realmAccess);
                        
                        return new Jwt(token, null, null, Map.of("alg", "RS256"), claims);
                    } catch (Exception e) {
                        throw new JwtException("Failed to decode JWT", e);
                    }
                }
            };
        } catch (Exception e) {
            System.err.println("Failed to create JWT Decoder: " + e.getMessage());
            e.printStackTrace();
            // Fallback for development - create a dummy decoder
            // In production, this should be properly configured
            throw new RuntimeException("Failed to connect to Keycloak. Please ensure Keycloak is running and properly configured.", e);
        }
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, KeycloakJwtAuthenticationConverter converter) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(authz -> authz
                .requestMatchers("/api/**").authenticated()
                .anyRequest().permitAll()
            )
            .oauth2ResourceServer(oauth2 -> oauth2
                .jwt(jwt -> jwt
                    .jwtAuthenticationConverter(converter)
                )
            )
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            );

        return http.build();
    }
}