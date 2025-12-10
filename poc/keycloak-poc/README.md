# Keycloak POC - User Registration and Client Role Management

This is a Proof of Concept (POC) project demonstrating user registration and client role management in Keycloak using the Java Admin Client.

## Prerequisites

- Java 11 or higher
- Maven
- Keycloak server running (configured in docker-compose.yml in parent directory)

## Setup

1. Ensure Keycloak is running via docker-compose in the parent directory:
   ```bash
   cd ..
   docker-compose up -d
   ```

2. Keycloak should be accessible at http://localhost:8081 with admin credentials: admin/admin

## Running the POC

1. Navigate to the POC directory:
   ```bash
   cd poc/keycloak-poc
   ```

2. Compile and run:
   ```bash
   mvn clean compile exec:java
   ```

## What the POC Demonstrates

### User Registration
- Creates a new user with username "testuser"
- Sets a password for the user
- Enables the user account

### Client Role Management
- Finds or creates a client (uses "account" client if exists, otherwise creates "test-client")
- Creates a new client role named "test-role"
- Assigns the role to the created user

## Configuration

The application connects to Keycloak with the following default configuration:
- Server URL: http://localhost:8081
- Realm: master
- Client ID: admin-cli
- Username: admin
- Password: admin

You can modify these values in the `KeycloakPOC.java` file.

## Dependencies

- Keycloak Admin Client: For interacting with Keycloak's Admin REST API
- RESTEasy Client: JAX-RS client implementation
- Jakarta WS RS API: JAX-RS API for REST operations
- SLF4J Simple: For logging

## Notes

- This POC uses the master realm for simplicity
- In production, you would typically use a different realm
- The created user and role are for demonstration purposes only