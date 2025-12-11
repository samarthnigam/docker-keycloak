package com.example.librarymanagement.service;

import com.example.librarymanagement.entity.*;
import com.example.librarymanagement.repository.CustomPermissionRepository;
import com.example.librarymanagement.repository.CustomPermissionRuleRepository;
import com.example.librarymanagement.repository.CustomRoleRepository;
import com.example.librarymanagement.repository.UserCustomRoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Transactional
public class RolePermissionService {

    @Autowired
    private CustomRoleRepository customRoleRepository;

    @Autowired
    private CustomPermissionRepository customPermissionRepository;

    @Autowired
    private UserCustomRoleRepository userCustomRoleRepository;

    @Autowired
    private CustomPermissionRuleRepository customPermissionRuleRepository;

    // CustomRole operations
    public List<CustomRole> findAllRoles() {
        return customRoleRepository.findAll();
    }

    public Optional<CustomRole> findRoleById(Long id) {
        return customRoleRepository.findById(id);
    }

    public Optional<CustomRole> findRoleByName(String name) {
        return customRoleRepository.findByName(name);
    }

    public CustomRole saveRole(CustomRole role) {
        if (customRoleRepository.existsByName(role.getName())) {
            throw new RuntimeException("Role with name '" + role.getName() + "' already exists");
        }
        return customRoleRepository.save(role);
    }

    public CustomRole updateRole(Long id, CustomRole roleDetails) {
        CustomRole role = customRoleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Role not found with id: " + id));

        // Check if name is being changed and if it conflicts
        if (!role.getName().equals(roleDetails.getName()) &&
            customRoleRepository.existsByName(roleDetails.getName())) {
            throw new RuntimeException("Role with name '" + roleDetails.getName() + "' already exists");
        }

        role.setName(roleDetails.getName());
        role.setDescription(roleDetails.getDescription());
        role.setPermissions(roleDetails.getPermissions());

        return customRoleRepository.save(role);
    }

    public void deleteRole(Long id) {
        CustomRole role = customRoleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Role not found with id: " + id));

        // Remove role assignments
        List<UserCustomRole> assignments = userCustomRoleRepository.findByCustomRole(role);
        userCustomRoleRepository.deleteAll(assignments);

        customRoleRepository.delete(role);
    }

    // CustomPermission operations
    public List<CustomPermission> findAllPermissions() {
        return customPermissionRepository.findAll();
    }

    public Optional<CustomPermission> findPermissionById(Long id) {
        return customPermissionRepository.findById(id);
    }

    public Optional<CustomPermission> findPermissionByName(String name) {
        return customPermissionRepository.findByName(name);
    }

    public CustomPermission savePermission(CustomPermission permission) {
        if (customPermissionRepository.existsByName(permission.getName())) {
            throw new RuntimeException("Permission with name '" + permission.getName() + "' already exists");
        }
        return customPermissionRepository.save(permission);
    }

    public CustomPermission updatePermission(Long id, CustomPermission permissionDetails) {
        CustomPermission permission = customPermissionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Permission not found with id: " + id));

        if (!permission.getName().equals(permissionDetails.getName()) &&
            customPermissionRepository.existsByName(permissionDetails.getName())) {
            throw new RuntimeException("Permission with name '" + permissionDetails.getName() + "' already exists");
        }

        permission.setName(permissionDetails.getName());
        permission.setDescription(permissionDetails.getDescription());
        permission.setResourceType(permissionDetails.getResourceType());
        permission.setAction(permissionDetails.getAction());

        return customPermissionRepository.save(permission);
    }

    public void deletePermission(Long id) {
        customPermissionRepository.deleteById(id);
    }

    // User Role Assignment operations
    public List<UserCustomRole> findAllUserRoles() {
        return userCustomRoleRepository.findAll();
    }

    public List<UserCustomRole> findRolesByUserId(String userId) {
        return userCustomRoleRepository.findByUserId(userId);
    }

    public List<UserCustomRole> findActiveRolesByUserId(String userId) {
        return userCustomRoleRepository.findActiveRolesByUserId(userId);
    }

    public UserCustomRole assignRoleToUser(String userId, String username, Long roleId) {
        CustomRole role = customRoleRepository.findById(roleId)
                .orElseThrow(() -> new RuntimeException("Role not found with id: " + roleId));

        if (userCustomRoleRepository.existsByUserIdAndCustomRole(userId, role)) {
            throw new RuntimeException("User already has this role assigned");
        }

        UserCustomRole assignment = new UserCustomRole();
        assignment.setUserId(userId);
        assignment.setUsername(username);
        assignment.setCustomRole(role);

        return userCustomRoleRepository.save(assignment);
    }

    public void removeRoleFromUser(String userId, Long roleId) {
        CustomRole role = customRoleRepository.findById(roleId)
                .orElseThrow(() -> new RuntimeException("Role not found with id: " + roleId));

        UserCustomRole assignment = userCustomRoleRepository.findByUserIdAndCustomRole(userId, role)
                .orElseThrow(() -> new RuntimeException("User does not have this role assigned"));

        userCustomRoleRepository.delete(assignment);
    }

    // Permission Rule operations
    public List<CustomPermissionRule> findAllPermissionRules() {
        return customPermissionRuleRepository.findAll();
    }

    public CustomPermissionRule savePermissionRule(CustomPermissionRule rule) {
        return customPermissionRuleRepository.save(rule);
    }

    public CustomPermissionRule updatePermissionRule(Long id, CustomPermissionRule ruleDetails) {
        CustomPermissionRule rule = customPermissionRuleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Permission rule not found"));

        rule.setName(ruleDetails.getName());
        rule.setDescription(ruleDetails.getDescription());
        rule.setResourceType(ruleDetails.getResourceType());
        rule.setResourceId(ruleDetails.getResourceId());
        rule.setEffect(ruleDetails.getEffect());
        rule.setConditionType(ruleDetails.getConditionType());
        rule.setConditionValue(ruleDetails.getConditionValue());

        return customPermissionRuleRepository.save(rule);
    }

    public void deletePermissionRule(Long id) {
        customPermissionRuleRepository.deleteById(id);
    }

    // Permission Evaluation
    public boolean hasPermission(String userId, CustomPermission.ResourceType resourceType,
                                Long resourceId, CustomPermission.PermissionAction action) {
        return hasPermission(userId, resourceType, resourceId, action, null);
    }

    public boolean hasPermission(String userId, CustomPermission.ResourceType resourceType,
                                Long resourceId, CustomPermission.PermissionAction action, Authentication authentication) {
        System.out.println("Checking permission for userId: " + userId + ", resourceType: " + resourceType + ", action: " + action);
        // Get user's custom roles from database
        List<UserCustomRole> userRoles = findActiveRolesByUserId(userId);
        System.out.println("Found " + userRoles.size() + " database roles for user: " + userId);

        // Check if any role has the required permission
        Set<CustomPermission> userPermissions = userRoles.stream()
                .flatMap(role -> role.getCustomRole().getPermissions().stream())
                .collect(Collectors.toSet());
        System.out.println("User has " + userPermissions.size() + " permissions from database roles");

        boolean hasPermission = userPermissions.stream()
                .anyMatch(perm -> perm.getResourceType() == resourceType &&
                                 (perm.getAction() == action || perm.getAction() == CustomPermission.PermissionAction.ALL));
        System.out.println("Database permission check result: " + hasPermission);

        // If no database roles found, check JWT roles
        if (!hasPermission && userRoles.isEmpty() && authentication != null) {
            System.out.println("No database roles found, checking JWT roles");
            hasPermission = checkJwtRolesForPermission(authentication, resourceType, action);
            System.out.println("JWT permission check result: " + hasPermission);
        }

        if (!hasPermission) {
            return false;
        }

        // Check for specific deny rules
        List<CustomPermissionRule> denyRules = customPermissionRuleRepository
                .findByResourceAndEffect(resourceType, resourceId, CustomPermissionRule.RuleEffect.DENY);

        for (CustomPermissionRule rule : denyRules) {
            if (matchesCondition(rule, userId)) {
                return false; // Explicit deny overrides allow
            }
        }

        return true;
    }

    private boolean checkJwtRolesForPermission(Authentication authentication, CustomPermission.ResourceType resourceType, CustomPermission.PermissionAction action) {
        // Extract roles from JWT authorities
        List<String> jwtRoles = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .filter(auth -> auth.startsWith("ROLE_"))
                .map(auth -> auth.substring(5)) // Remove "ROLE_" prefix
                .collect(Collectors.toList());

        // Check if any JWT role matches a database role with the required permission
        for (String roleName : jwtRoles) {
            Optional<CustomRole> roleOpt = findRoleByName(roleName);
            if (roleOpt.isPresent()) {
                CustomRole role = roleOpt.get();
                boolean hasPermission = role.getPermissions().stream()
                        .anyMatch(perm -> perm.getResourceType() == resourceType &&
                                         (perm.getAction() == action || perm.getAction() == CustomPermission.PermissionAction.ALL));
                if (hasPermission) {
                    return true;
                }
            }
        }

        return false;
    }

    // Overloaded hasPermission method that takes string parameters
    public boolean hasPermission(Long userId, String permission, String resourceType, Long resourceId, Map<String, Object> context) {
        return hasPermission(String.valueOf(userId), permission, resourceType, resourceId, context);
    }

    public boolean hasPermission(String userId, String permission, String resourceType, Long resourceId, Map<String, Object> context) {
        return hasPermission(userId, permission, resourceType, resourceId, context, null);
    }

    public boolean hasPermission(String userId, String permission, String resourceType, Long resourceId, Map<String, Object> context, Authentication authentication) {
        try {
            // Convert string parameters to enums
            CustomPermission.ResourceType rt = CustomPermission.ResourceType.valueOf(resourceType);
            CustomPermission.PermissionAction action = mapPermissionToAction(permission);

            return hasPermission(userId, rt, resourceId, action, authentication);
        } catch (IllegalArgumentException e) {
            // If conversion fails, deny permission
            return false;
        }
    }

    private CustomPermission.PermissionAction mapPermissionToAction(String permission) {
        // Map permission strings to actions
        switch (permission.toUpperCase()) {
            case "READ_BOOK":
            case "READ_USER":
            case "READ_ORDER":
                return CustomPermission.PermissionAction.READ;
            case "CREATE_BOOK":
            case "CREATE_USER":
            case "CREATE_ORDER":
                return CustomPermission.PermissionAction.WRITE;
            case "UPDATE_BOOK":
            case "UPDATE_USER":
                return CustomPermission.PermissionAction.WRITE;
            case "DELETE_BOOK":
            case "DELETE_USER":
                return CustomPermission.PermissionAction.DELETE;
            case "RENT_BOOK":
            case "RETURN_BOOK":
                return CustomPermission.PermissionAction.WRITE;
            default:
                return CustomPermission.PermissionAction.ALL;
        }
    }

    private boolean matchesCondition(CustomPermissionRule rule, String userId) {
        switch (rule.getConditionType()) {
            case USER_ID:
                return userId.equals(rule.getConditionValue());
            case USERNAME:
                // This would require additional user info, for now return false
                return false;
            case USER_ROLE:
                // Check if user has the specified role
                List<UserCustomRole> userRoles = findActiveRolesByUserId(userId);
                return userRoles.stream()
                        .anyMatch(ur -> ur.getCustomRole().getName().equals(rule.getConditionValue()));
            case CUSTOM_CONDITION:
                // For custom conditions, we'd need a more sophisticated evaluation
                // For now, support simple conditions like "user_id=2"
                return evaluateCustomCondition(rule.getConditionValue(), userId);
            default:
                return false;
        }
    }

    private boolean evaluateCustomCondition(String condition, String userId) {
        // Simple condition evaluation - can be extended
        if (condition.startsWith("user_id=")) {
            String expectedUserId = condition.substring("user_id=".length());
            return userId.equals(expectedUserId);
        }
        return false;
    }

    // Get all permissions for a user
    public Set<String> getUserPermissions(String userId) {
        List<UserCustomRole> userRoles = findActiveRolesByUserId(userId);

        return userRoles.stream()
                .flatMap(role -> role.getCustomRole().getPermissions().stream())
                .map(CustomPermission::getName)
                .collect(Collectors.toSet());
    }

    // Get all roles for a user
    public Set<String> getUserRoles(String userId) {
        return findActiveRolesByUserId(userId).stream()
                .map(role -> role.getCustomRole().getName())
                .collect(Collectors.toSet());
    }

    // Get all role names for a user by ID (for JWT authentication converter)
    public List<String> getUserRoleNames(String userId) {
        return findActiveRolesByUserId(userId).stream()
                .map(role -> role.getCustomRole().getName())
                .collect(Collectors.toList());
    }

    // Assign a role to a user
    public void assignRoleToUser(String userId, Long roleId) {
        CustomRole role = customRoleRepository.findById(roleId)
                .orElseThrow(() -> new RuntimeException("Role not found"));

        // Check if user already has this role
        boolean alreadyHasRole = userCustomRoleRepository.existsByUserIdAndCustomRole(userId, role);
        if (alreadyHasRole) {
            throw new RuntimeException("User already has this role");
        }

        UserCustomRole userRole = new UserCustomRole();
        userRole.setUserId(userId);
        userRole.setCustomRole(role);
        userCustomRoleRepository.save(userRole);
    }
}