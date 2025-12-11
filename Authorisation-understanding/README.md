# Keycloak Authorization Demo with Spring Boot 3.x

This project demonstrates how to implement ID Token exploration and Keycloak-based authorization in a Spring Boot 3.x application using OAuth2 Resource Server.

## 🎯 What This Demo Shows

### 1. **ID Token vs Access Token**
- **ID Token**: Contains user identity information (name, email, etc.) - consumed by client applications
- **Access Token**: Contains authorization data (roles, scopes) - consumed by resource servers

### 2. **Keycloak Role-Based Authorization**
- Extract roles from Keycloak JWT tokens
- Map Keycloak roles to Spring Security authorities
- Implement role-based access control

### 3. **Spring Security OAuth2 Integration**
- JWT validation using Keycloak's JWKS endpoint
- Custom role converter for Keycloak tokens
- Method-level security with `@PreAuthorize`

## 🏗️ Project Structure

```
src/main/java/com/example/demo/
├── DemoApplication.java              # Main Spring Boot application
├── config/
│   ├── SecurityConfig.java           # Security configuration
│   └── KeycloakRoleConverter.java    # Custom JWT role converter
└── controller/
    ├── PublicController.java         # Public endpoints (no auth)
    ├── UserController.java           # User endpoints (authenticated)
    ├── AdminController.java          # Admin endpoints (ADMIN role)
    └── TokenInfoController.java      # Token exploration endpoint
```

## 🚀 Quick Start

### Prerequisites
- Java 17+
- Maven 3.6+
- Docker (for Keycloak)

### Step 1: Start Keycloak

```bash
# Start Keycloak using Docker
docker run -p 8081:8080 -e KEYCLOAK_ADMIN=admin -e KEYCLOAK_ADMIN_PASSWORD=admin quay.io/keycloak/keycloak:latest start-dev
```

### Step 2: Import Demo Realm

1. Open Keycloak Admin Console: http://localhost:8081
2. Login with `admin` / `admin`
3. Go to "Master" realm → "Import"
4. Upload `demo-realm.json` from this project
5. Import the realm

### Step 3: Run Spring Boot Application

```bash
cd Authorisation-understanding
mvn spring-boot:run
```

The application will start on port 8080.

## 🧪 Testing the Endpoints

### 1. Public Endpoint (No Authentication Required)

```bash
curl http://localhost:8080/public
```

**Expected Response:**
```json
{
  "message": "This is a public endpoint - no authentication required!",
  "timestamp": 1702291200000,
  "status": "success"
}
```

### 2. Get Access Token from Keycloak

First, get an access token for the test user:

```bash
curl -X POST http://localhost:8081/realms/demo-realm/protocol/openid-connect/token \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "grant_type=password" \
  -d "client_id=spring-demo-app" \
  -d "client_secret=demo-secret-key" \
  -d "username=testuser" \
  -d "password=password"
```

**Response contains:**
```json
{
  "access_token": "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9...",
  "token_type": "Bearer",
  "expires_in": 300,
  "refresh_token": "...",
  "id_token": "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9..."
}
```

### 3. Test User Endpoint (Requires Authentication)

```bash
curl -H "Authorization: Bearer YOUR_ACCESS_TOKEN" \
     http://localhost:8080/user
```

**Expected Response:**
```json
{
  "message": "Welcome! You are authenticated.",
  "username": "testuser",
  "authorities": ["ROLE_USER"],
  "timestamp": 1702291200000
}
```

### 4. Test Admin Endpoint (Requires ADMIN Role)

```bash
curl -H "Authorization: Bearer YOUR_ACCESS_TOKEN" \
     http://localhost:8080/admin
```

**Expected Response (403 Forbidden):**
```json
{
  "error": "Forbidden",
  "message": "Access Denied",
  "path": "/admin"
}
```

### 5. Explore Token Information

```bash
curl -H "Authorization: Bearer YOUR_ACCESS_TOKEN" \
     http://localhost:8080/token-info
```

**Response shows:**
- Complete JWT claims
- Identity claims (from ID Token context)
- Authorization claims (from Access Token)
- Token metadata
- Explanations of ID Token vs Access Token

## 🔍 Understanding the Code

### SecurityConfig.java
```java
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
            .authorizeHttpRequests(authz -> authz
                .requestMatchers("/public").permitAll()
                .requestMatchers("/user/**").authenticated()
                .requestMatchers("/admin/**").hasRole("ADMIN")
                .anyRequest().authenticated()
            )
            .oauth2ResourceServer(oauth2 -> oauth2
                .jwt(jwt -> jwt
                    .jwtAuthenticationConverter(jwtAuthenticationConverter())
                )
            )
            .build();
    }

    @Bean
    public JwtDecoder jwtDecoder() {
        return NimbusJwtDecoder.withJwkSetUri(
            "http://localhost:8081/realms/demo-realm/protocol/openid-connect/certs"
        ).build();
    }
}
```

### KeycloakRoleConverter.java
```java
@Component
public class KeycloakRoleConverter implements Converter<Jwt, Collection<GrantedAuthority>> {

    @Override
    public Collection<GrantedAuthority> convert(Jwt jwt) {
        Collection<GrantedAuthority> authorities = new ArrayList<>();

        Map<String, Object> realmAccess = jwt.getClaim("realm_access");
        if (realmAccess != null && realmAccess.containsKey("roles")) {
            @SuppressWarnings("unchecked")
            List<String> roles = (List<String>) realmAccess.get("roles");

            for (String role : roles) {
                authorities.add(new SimpleGrantedAuthority("ROLE_" + role));
            }
        }

        return authorities;
    }
}
```

## 📚 Key Concepts Explained

### ID Token vs Access Token

| Aspect | ID Token | Access Token |
|--------|----------|--------------|
| Purpose | User identity | API authorization |
| Consumer | Client application | Resource server |
| Contains | User info (name, email) | Roles, scopes, permissions |
| Format | JWT | JWT |
| Validation | By client | By resource server |

### Keycloak Roles in JWT

Keycloak stores roles in the `realm_access` claim:

```json
{
  "realm_access": {
    "roles": ["USER", "ADMIN"]
  }
}
```

Our `KeycloakRoleConverter` extracts these and converts them to Spring Security authorities with the `ROLE_` prefix.

### Spring Security Integration

1. **JWT Validation**: Uses Keycloak's JWKS endpoint
2. **Role Extraction**: Custom converter maps Keycloak roles to authorities
3. **Authorization**: Uses standard Spring Security annotations and method security

## 🐛 Troubleshooting

### Common Issues

1. **"Invalid token" errors**: Check that Keycloak is running and realm is imported correctly
2. **403 Forbidden on /admin**: User needs ADMIN role (testuser only has USER role)
3. **Connection refused**: Ensure Keycloak is running on port 8081

### Debug Logging

Enable debug logging in `application.properties`:
```properties
logging.level.org.springframework.security=DEBUG
logging.level.org.springframework.web=DEBUG
```

### H2 Console

Access H2 database console at: http://localhost:8080/h2-console
- JDBC URL: `jdbc:h2:mem:testdb`
- Username: `sa`
- Password: `password`

## 📋 Next Steps

1. **Add ADMIN user**: Create a user with ADMIN role to test admin endpoints
2. **Client credentials flow**: Implement service-to-service authentication
3. **Method-level security**: Use `@PreAuthorize` for fine-grained access control
4. **Custom claims**: Extract additional user attributes from JWT
5. **Token refresh**: Handle token expiration and refresh

## 🔗 Useful Links

- [Keycloak Documentation](https://www.keycloak.org/documentation)
- [Spring Security OAuth2](https://docs.spring.io/spring-security/reference/servlet/oauth2/index.html)
- [OAuth2 Specification](https://tools.ietf.org/html/rfc6749)
- [OpenID Connect](https://openid.net/connect/)