package com.example.librarymanagement.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Entity
@Table(name = "custom_permission_rules")
public class CustomPermissionRule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Rule name is required")
    @Column(unique = true)
    private String name;

    private String description;

    @Enumerated(EnumType.STRING)
    @NotNull(message = "Resource type is required")
    private CustomPermission.ResourceType resourceType;

    @NotNull(message = "Resource ID is required")
    private Long resourceId; // ID of the specific resource (book, user, etc.)

    @Enumerated(EnumType.STRING)
    @NotNull(message = "Effect is required")
    private RuleEffect effect; // ALLOW or DENY

    @Enumerated(EnumType.STRING)
    @NotNull(message = "Condition type is required")
    private ConditionType conditionType;

    private String conditionValue; // e.g., "user_id=2", "age<10", etc.

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // Enums
    public enum RuleEffect {
        ALLOW, DENY
    }

    public enum ConditionType {
        USER_ID, USERNAME, USER_ROLE, CUSTOM_CONDITION
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public CustomPermission.ResourceType getResourceType() {
        return resourceType;
    }

    public void setResourceType(CustomPermission.ResourceType resourceType) {
        this.resourceType = resourceType;
    }

    public Long getResourceId() {
        return resourceId;
    }

    public void setResourceId(Long resourceId) {
        this.resourceId = resourceId;
    }

    public RuleEffect getEffect() {
        return effect;
    }

    public void setEffect(RuleEffect effect) {
        this.effect = effect;
    }

    public ConditionType getConditionType() {
        return conditionType;
    }

    public void setConditionType(ConditionType conditionType) {
        this.conditionType = conditionType;
    }

    public String getConditionValue() {
        return conditionValue;
    }

    public void setConditionValue(String conditionValue) {
        this.conditionValue = conditionValue;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}