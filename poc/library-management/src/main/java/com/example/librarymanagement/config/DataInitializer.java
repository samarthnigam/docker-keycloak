package com.example.librarymanagement.config;

import com.example.librarymanagement.entity.CustomPermission;
import com.example.librarymanagement.entity.CustomRole;
import com.example.librarymanagement.repository.CustomPermissionRepository;
import com.example.librarymanagement.repository.CustomRoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private CustomRoleRepository roleRepository;

    @Autowired
    private CustomPermissionRepository permissionRepository;

    @Override
    public void run(String... args) throws Exception {
        // Create basic permissions if they don't exist
        createPermissionsIfNotExist();

        // Create basic roles if they don't exist
        createRolesIfNotExist();
    }

    private void createPermissionsIfNotExist() {
        List<CustomPermission> permissions = Arrays.asList(
            createPermission("READ_BOOK", "Permission to read/view books", "BOOK"),
            createPermission("CREATE_BOOK", "Permission to create new books", "BOOK"),
            createPermission("UPDATE_BOOK", "Permission to update book information", "BOOK"),
            createPermission("DELETE_BOOK", "Permission to delete books", "BOOK"),
            createPermission("RENT_BOOK", "Permission to rent books", "RENTAL"),
            createPermission("RETURN_BOOK", "Permission to return rented books", "RENTAL"),
            createPermission("CREATE_USER", "Permission to create user accounts", "USER"),
            createPermission("UPDATE_USER", "Permission to update user information", "USER"),
            createPermission("DELETE_USER", "Permission to delete users", "USER"),
            createPermission("READ_ORDER", "Permission to view orders", "ORDER"),
            createPermission("CREATE_ORDER", "Permission to create orders", "ORDER")
        );

        for (CustomPermission permission : permissions) {
            if (!permissionRepository.existsByName(permission.getName())) {
                permissionRepository.save(permission);
            }
        }
    }

    private void createRolesIfNotExist() {
        // Librarian role
        if (!roleRepository.existsByName("Librarian")) {
            CustomRole librarianRole = new CustomRole();
            librarianRole.setName("Librarian");
            librarianRole.setDescription("Library staff with book management permissions");

            List<CustomPermission> librarianPermissions = permissionRepository.findAllByNameIn(Arrays.asList(
                "READ_BOOK", "CREATE_BOOK", "UPDATE_BOOK", "DELETE_BOOK",
                "RENT_BOOK", "RETURN_BOOK", "READ_ORDER", "CREATE_ORDER"
            ));
            librarianRole.setPermissions(new HashSet<>(librarianPermissions));

            roleRepository.save(librarianRole);
        }

        // Student role
        if (!roleRepository.existsByName("Student")) {
            CustomRole studentRole = new CustomRole();
            studentRole.setName("Student");
            studentRole.setDescription("Students with basic library access");

            List<CustomPermission> studentPermissions = permissionRepository.findAllByNameIn(Arrays.asList(
                "READ_BOOK", "RENT_BOOK", "RETURN_BOOK"
            ));
            studentRole.setPermissions(new HashSet<>(studentPermissions));

            roleRepository.save(studentRole);
        }

        // Teacher role
        if (!roleRepository.existsByName("Teacher")) {
            CustomRole teacherRole = new CustomRole();
            teacherRole.setName("Teacher");
            teacherRole.setDescription("Teachers with extended library access");

            List<CustomPermission> teacherPermissions = permissionRepository.findAllByNameIn(Arrays.asList(
                "READ_BOOK", "RENT_BOOK", "RETURN_BOOK", "CREATE_ORDER"
            ));
            teacherRole.setPermissions(new HashSet<>(teacherPermissions));

            roleRepository.save(teacherRole);
        }

        // Admin role
        if (!roleRepository.existsByName("Admin")) {
            CustomRole adminRole = new CustomRole();
            adminRole.setName("Admin");
            adminRole.setDescription("Administrators with full system access");

            List<CustomPermission> adminPermissions = permissionRepository.findAll();
            adminRole.setPermissions(new HashSet<>(adminPermissions));

            roleRepository.save(adminRole);
        }

        // BookManager role (custom role example)
        if (!roleRepository.existsByName("BookManager")) {
            CustomRole bookManagerRole = new CustomRole();
            bookManagerRole.setName("BookManager");
            bookManagerRole.setDescription("Specialized role for book catalog management");

            List<CustomPermission> bookManagerPermissions = permissionRepository.findAllByNameIn(Arrays.asList(
                "READ_BOOK", "CREATE_BOOK", "UPDATE_BOOK", "READ_ORDER"
            ));
            bookManagerRole.setPermissions(new HashSet<>(bookManagerPermissions));

            roleRepository.save(bookManagerRole);
        }
    }

    private CustomPermission createPermission(String name, String description, String resourceType) {
        CustomPermission permission = new CustomPermission();
        permission.setName(name);
        permission.setDescription(description);
        if (resourceType != null) {
            permission.setResourceType(CustomPermission.ResourceType.valueOf(resourceType));
        }
        
        // Set action based on permission name
        if (name.startsWith("READ_")) {
            permission.setAction(CustomPermission.PermissionAction.READ);
        } else if (name.startsWith("CREATE_") || name.startsWith("UPDATE_")) {
            permission.setAction(CustomPermission.PermissionAction.WRITE);
        } else if (name.startsWith("DELETE_")) {
            permission.setAction(CustomPermission.PermissionAction.DELETE);
        } else {
            permission.setAction(CustomPermission.PermissionAction.ALL);
        }
        
        return permission;
    }
}