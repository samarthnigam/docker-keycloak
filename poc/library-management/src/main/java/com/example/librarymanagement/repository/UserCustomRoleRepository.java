package com.example.librarymanagement.repository;

import com.example.librarymanagement.entity.CustomRole;
import com.example.librarymanagement.entity.UserCustomRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserCustomRoleRepository extends JpaRepository<UserCustomRole, Long> {

    List<UserCustomRole> findByUserId(String userId);

    List<UserCustomRole> findByUsername(String username);

    List<UserCustomRole> findByCustomRole(CustomRole customRole);

    Optional<UserCustomRole> findByUserIdAndCustomRole(String userId, CustomRole customRole);

    @Query("SELECT ucr FROM UserCustomRole ucr WHERE ucr.userId = :userId AND (ucr.expiresAt IS NULL OR ucr.expiresAt > CURRENT_TIMESTAMP)")
    List<UserCustomRole> findActiveRolesByUserId(@Param("userId") String userId);

    boolean existsByUserIdAndCustomRole(String userId, CustomRole customRole);
}