package com.example.demo.config;

import org.springframework.core.convert.converter.Converter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Custom converter to extract roles from Keycloak JWT tokens
 * and convert them to Spring Security GrantedAuthority objects.
 *
 * Keycloak stores roles in the "realm_access" claim with a "roles" array.
 */
@Component
public class KeycloakRoleConverter implements Converter<Jwt, Collection<GrantedAuthority>> {

    @Override
    public Collection<GrantedAuthority> convert(Jwt jwt) {
        Collection<GrantedAuthority> grantedAuthorities = new ArrayList<>();

        // Extract roles from realm_access claim
        Map<String, Object> realmAccess = jwt.getClaim("realm_access");
        if (realmAccess != null && realmAccess.containsKey("roles")) {
            @SuppressWarnings("unchecked")
            List<String> roles = (List<String>) realmAccess.get("roles");

            for (String role : roles) {
                // Convert Keycloak roles to Spring Security authorities
                // Prefix with "ROLE_" to match Spring Security conventions
                grantedAuthorities.add(new SimpleGrantedAuthority("ROLE_" + role));
            }
        }

        return grantedAuthorities;
    }
}