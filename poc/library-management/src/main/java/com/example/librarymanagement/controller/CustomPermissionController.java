package com.example.librarymanagement.controller;

import com.example.librarymanagement.entity.CustomPermission;
import com.example.librarymanagement.service.RolePermissionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/permissions")
public class CustomPermissionController {

    @Autowired
    private RolePermissionService rolePermissionService;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<CustomPermission>> getAllPermissions() {
        List<CustomPermission> permissions = rolePermissionService.findAllPermissions();
        return ResponseEntity.ok(permissions);
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<CustomPermission> getPermissionById(@PathVariable Long id) {
        return rolePermissionService.findPermissionById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<CustomPermission> createPermission(@Valid @RequestBody CustomPermission permission) {
        CustomPermission savedPermission = rolePermissionService.savePermission(permission);
        return ResponseEntity.ok(savedPermission);
    }

    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<CustomPermission> updatePermission(@PathVariable Long id, @Valid @RequestBody CustomPermission permissionDetails) {
        try {
            CustomPermission updatedPermission = rolePermissionService.updatePermission(id, permissionDetails);
            return ResponseEntity.ok(updatedPermission);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> deletePermission(@PathVariable Long id) {
        try {
            rolePermissionService.deletePermission(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
}