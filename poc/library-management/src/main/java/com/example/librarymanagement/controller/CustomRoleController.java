package com.example.librarymanagement.controller;

import com.example.librarymanagement.entity.CustomRole;
import com.example.librarymanagement.service.RolePermissionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/roles")
public class CustomRoleController {

    @Autowired
    private RolePermissionService rolePermissionService;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<CustomRole>> getAllRoles() {
        List<CustomRole> roles = rolePermissionService.findAllRoles();
        return ResponseEntity.ok(roles);
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<CustomRole> getRoleById(@PathVariable Long id) {
        return rolePermissionService.findRoleById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<CustomRole> createRole(@Valid @RequestBody CustomRole role) {
        CustomRole savedRole = rolePermissionService.saveRole(role);
        return ResponseEntity.ok(savedRole);
    }

    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<CustomRole> updateRole(@PathVariable Long id, @Valid @RequestBody CustomRole roleDetails) {
        try {
            CustomRole updatedRole = rolePermissionService.updateRole(id, roleDetails);
            return ResponseEntity.ok(updatedRole);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/{roleId}/assign/{userId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<String> assignRoleToUser(@PathVariable Long roleId, @PathVariable String userId) {
        try {
            rolePermissionService.assignRoleToUser(userId, roleId);
            return ResponseEntity.ok("Role assigned successfully");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body("Failed to assign role: " + e.getMessage());
        }
    }
}