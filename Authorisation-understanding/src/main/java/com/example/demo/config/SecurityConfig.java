package com.example.demo.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Security configuration for OAuth2 Resource Server with Keycloak JWT validation.
 *
 * This configuration:
 * - Sets up JWT validation using Keycloak's JWKS endpoint
 * - Configures role extraction from JWT tokens
 * - Defines HTTP security rules for different endpoints
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final KeycloakRoleConverter keycloakRoleConverter;

    public SecurityConfig(KeycloakRoleConverter keycloakRoleConverter) {
        this.keycloakRoleConverter = keycloakRoleConverter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // Disable CSRF for stateless API
            .csrf(csrf -> csrf.disable())

            // Configure session management for stateless authentication
            .sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )

            // Configure authorization rules
            .authorizeHttpRequests(authz -> authz
                // Public endpoints - no authentication required
                .requestMatchers("/api/public", "/h2-console/**").permitAll()

                // User endpoints - require authentication
                .requestMatchers("/api/user/**").authenticated()

                // Admin endpoints - require ADMIN role
                .requestMatchers("/api/admin/**").hasRole("ADMIN")

                // Token info endpoint - require authentication
                .requestMatchers("/api/token-info").authenticated()

                // All other requests require authentication
                .anyRequest().authenticated()
            )

            // Configure OAuth2 Resource Server with JWT
            .oauth2ResourceServer(oauth2 -> oauth2
                .jwt(jwt -> jwt
                    .jwtAuthenticationConverter(jwtAuthenticationConverter())
                )
            );

        return http.build();
    }

    /**
     * Configure JWT decoder to validate tokens using Keycloak's JWKS endpoint.
     */
    @Bean
    public JwtDecoder jwtDecoder() {
        // Use Keycloak's JWKS endpoint for token validation
        return NimbusJwtDecoder.withJwkSetUri("http://localhost:8081/realms/demo-realm/protocol/openid-connect/certs")
                .build();
    }

    /**
     * Configure JWT authentication converter to extract authorities from JWT.
     */
    private JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();

        // Use our custom role converter to extract roles from Keycloak tokens
        converter.setJwtGrantedAuthoritiesConverter(keycloakRoleConverter);

        return converter;
    }
}