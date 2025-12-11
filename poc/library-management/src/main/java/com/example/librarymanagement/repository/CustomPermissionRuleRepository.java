package com.example.librarymanagement.repository;

import com.example.librarymanagement.entity.CustomPermission;
import com.example.librarymanagement.entity.CustomPermissionRule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CustomPermissionRuleRepository extends JpaRepository<CustomPermissionRule, Long> {

    List<CustomPermissionRule> findByResourceType(CustomPermission.ResourceType resourceType);

    List<CustomPermissionRule> findByResourceId(Long resourceId);

    List<CustomPermissionRule> findByResourceTypeAndResourceId(CustomPermission.ResourceType resourceType, Long resourceId);

    List<CustomPermissionRule> findByConditionType(CustomPermissionRule.ConditionType conditionType);

    @Query("SELECT cpr FROM CustomPermissionRule cpr WHERE cpr.resourceType = :resourceType AND cpr.resourceId = :resourceId AND cpr.effect = :effect")
    List<CustomPermissionRule> findByResourceAndEffect(@Param("resourceType") CustomPermission.ResourceType resourceType,
                                                      @Param("resourceId") Long resourceId,
                                                      @Param("effect") CustomPermissionRule.RuleEffect effect);

    @Query("SELECT cpr FROM CustomPermissionRule cpr WHERE cpr.conditionType = :conditionType AND cpr.conditionValue = :conditionValue")
    List<CustomPermissionRule> findByCondition(@Param("conditionType") CustomPermissionRule.ConditionType conditionType,
                                              @Param("conditionValue") String conditionValue);
}