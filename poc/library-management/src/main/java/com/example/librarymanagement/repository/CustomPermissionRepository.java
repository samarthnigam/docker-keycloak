package com.example.librarymanagement.repository;

import com.example.librarymanagement.entity.CustomPermission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CustomPermissionRepository extends JpaRepository<CustomPermission, Long> {

    Optional<CustomPermission> findByName(String name);

    boolean existsByName(String name);

    List<CustomPermission> findByResourceType(CustomPermission.ResourceType resourceType);

    List<CustomPermission> findByAction(CustomPermission.PermissionAction action);

    List<CustomPermission> findAllByNameIn(List<String> names);
}