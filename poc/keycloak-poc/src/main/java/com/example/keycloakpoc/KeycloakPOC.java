package com.example.keycloakpoc;

import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.keycloak.admin.client.resource.*;
import org.keycloak.representations.idm.*;
import jakarta.ws.rs.core.Response;

import java.util.Arrays;
import java.util.List;

public class KeycloakPOC {

    public static void main(String[] args) {
        // Configuration
        String serverUrl = "http://localhost:8081";
        String realm = "master";
        String clientId = "admin-cli";
        String username = "admin";
        String password = "admin";

        // Build Keycloak instance
        Keycloak keycloak = KeycloakBuilder.builder()
                .serverUrl(serverUrl)
                .realm(realm)
                .clientId(clientId)
                .username(username)
                .password(password)
                .build();

        try {
            // Get realm resource
            RealmResource realmResource = keycloak.realm(realm);

            // Demonstrate user registration
            System.out.println("=== User Registration Demo ===");
            createUser(realmResource);

            // Create library users with different roles
            System.out.println("\n=== Creating Library Users ===");
            createLibraryUsers(realmResource);

            // Demonstrate client role management
            System.out.println("\n=== Client Role Management Demo ===");
            manageClientRoles(realmResource);

        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        } finally {
            keycloak.close();
        }
    }

    private static void createUser(RealmResource realmResource) {
        UsersResource usersResource = realmResource.users();

        // Check if user already exists
        List<UserRepresentation> existingUsers = usersResource.search("testuser");
        if (!existingUsers.isEmpty()) {
            System.out.println("User 'testuser' already exists");
            return;
        }

        // Create a new user
        UserRepresentation user = new UserRepresentation();
        user.setUsername("testuser");
        user.setFirstName("Test");
        user.setLastName("User");
        user.setEmail("testuser@example.com");
        user.setEnabled(true);

        // Create user
        Response response = usersResource.create(user);
        if (response.getStatus() == 201) {
            System.out.println("User created with status: " + response.getStatus());

            // Get user ID from location header
            String userId = response.getLocation().getPath().replaceAll(".*/([^/]+)$", "$1");
            System.out.println("User ID: " + userId);

            // Set password (required for login)
            CredentialRepresentation credential = new CredentialRepresentation();
            credential.setType(CredentialRepresentation.PASSWORD);
            credential.setValue("password123");
            credential.setTemporary(false);

            usersResource.get(userId).resetPassword(credential);
            System.out.println("Password set for user");
        } else {
            System.out.println("Failed to create user, status: " + response.getStatus());
        }
    }

    private static void createLibraryUsers(RealmResource realmResource) {
        // Define users with their roles
        String[][] users = {
            {"admin", "admin", "Administrator", "Admin", "admin@library.com", "admin"},
            {"librarian", "librarian", "John", "Librarian", "librarian@library.com", "librarian"},
            {"student", "student", "Jane", "Student", "student@library.com", "student"},
            {"teacher", "teacher", "Prof", "Smith", "teacher@library.com", "teacher"},
            {"owner", "owner", "Bob", "Owner", "owner@library.com", "owner"}
        };

        UsersResource usersResource = realmResource.users();
        RolesResource rolesResource = realmResource.roles();

        for (String[] userData : users) {
            String username = userData[0];
            String password = userData[1];
            String firstName = userData[2];
            String lastName = userData[3];
            String email = userData[4];
            String roleName = userData[5];

            try {
                // Check if user already exists
                List<UserRepresentation> existingUsers = usersResource.search(username);
                if (!existingUsers.isEmpty()) {
                    System.out.println("User already exists: " + username);
                    continue;
                }

                // Create user
                UserRepresentation user = new UserRepresentation();
                user.setUsername(username);
                user.setFirstName(firstName);
                user.setLastName(lastName);
                user.setEmail(email);
                user.setEnabled(true);

                Response response = usersResource.create(user);
                String userId = response.getLocation().getPath().replaceAll(".*/([^/]+)$", "$1");

                // Set password
                CredentialRepresentation credential = new CredentialRepresentation();
                credential.setType(CredentialRepresentation.PASSWORD);
                credential.setValue(password);
                credential.setTemporary(false);

                usersResource.get(userId).resetPassword(credential);

                // Assign role
                try {
                    RoleRepresentation role = rolesResource.get(roleName).toRepresentation();
                    UserResource userResource = usersResource.get(userId);
                    userResource.roles().realmLevel().add(Arrays.asList(role));
                    System.out.println("Created user '" + username + "' with role '" + roleName + "'");
                } catch (Exception e) {
                    System.out.println("Role '" + roleName + "' not found for user '" + username + "'");
                }

            } catch (Exception e) {
                System.out.println("Error creating user '" + username + "': " + e.getMessage());
            }
        }
    }

    private static void manageClientRoles(RealmResource realmResource) {
        ClientsResource clientsResource = realmResource.clients();

        // Create library-app client
        createClient(clientsResource, "library-app");

        // Create library-api client
        createClient(clientsResource, "library-api");

        // Get or create account client for roles
        ClientRepresentation client = clientsResource.findAll().stream()
                .filter(c -> "account".equals(c.getClientId()))
                .findFirst()
                .orElse(null);

        if (client == null) {
            // Create a test client
            client = new ClientRepresentation();
            client.setClientId("library-client");
            client.setName("Library Client");
            client.setEnabled(true);
            client.setProtocol("openid-connect");

            Response response = clientsResource.create(client);
            String clientId = response.getLocation().getPath().replaceAll(".*/([^/]+)$", "$1");
            client = clientsResource.get(clientId).toRepresentation();
            System.out.println("Created library client: " + client.getClientId());
        }

        // Create realm roles
        createRealmRoles(realmResource);

        // Create client roles
        createClientRoles(realmResource, client);
    }

    private static void createClient(ClientsResource clientsResource, String clientId) {
        // Check if client already exists
        List<ClientRepresentation> existingClients = clientsResource.findAll();
        boolean clientExists = existingClients.stream()
                .anyMatch(c -> clientId.equals(c.getClientId()));

        if (!clientExists) {
            ClientRepresentation client = new ClientRepresentation();
            client.setClientId(clientId);
            client.setName(clientId + " Client");
            client.setEnabled(true);
            client.setProtocol("openid-connect");
            client.setPublicClient(true);
            client.setDirectAccessGrantsEnabled(true);

            clientsResource.create(client);
            System.out.println("Created client: " + clientId);
        } else {
            System.out.println("Client already exists: " + clientId);
        }
    }

    private static void createRealmRoles(RealmResource realmResource) {
        RolesResource rolesResource = realmResource.roles();

        // Define roles and their descriptions
        String[][] roles = {
            {"admin", "Administrator - Full access to all library functions"},
            {"librarian", "Librarian - Can view and edit book status"},
            {"student", "Student - Can view and rent books"},
            {"teacher", "Teacher - Can view and rent books"},
            {"owner", "Library Owner - Can request new book orders"}
        };

        for (String[] roleData : roles) {
            String roleName = roleData[0];
            String description = roleData[1];

            try {
                RoleRepresentation existingRole = rolesResource.get(roleName).toRepresentation();
                System.out.println("Role already exists: " + roleName);
            } catch (Exception e) {
                RoleRepresentation role = new RoleRepresentation();
                role.setName(roleName);
                role.setDescription(description);

                rolesResource.create(role);
                System.out.println("Created realm role: " + roleName);
            }
        }
    }

    private static void createClientRoles(RealmResource realmResource, ClientRepresentation client) {
        ClientsResource clientsResource = realmResource.clients();
        ClientResource clientResource = clientsResource.get(client.getId());
        RolesResource rolesResource = clientResource.roles();

        // Create client-specific roles if needed
        String[] clientRoles = {"book_viewer", "book_editor", "book_renter", "order_requester"};

        for (String roleName : clientRoles) {
            try {
                RoleRepresentation existingRole = rolesResource.get(roleName).toRepresentation();
                System.out.println("Client role already exists: " + roleName);
            } catch (Exception e) {
                RoleRepresentation role = new RoleRepresentation();
                role.setName(roleName);
                role.setDescription("Client role for " + roleName);

                rolesResource.create(role);
                System.out.println("Created client role: " + roleName);
            }
        }
    }
}