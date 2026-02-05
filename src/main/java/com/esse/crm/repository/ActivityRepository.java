package com.esse.crm.repository;

import com.esse.crm.dto.activity.ActivityType;
import com.esse.crm.entity.Activity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public interface ActivityRepository extends JpaRepository<Activity, Long> {

    @Query("SELECT a FROM Activity a WHERE " +
            "(:completed IS NULL OR a.completed = :completed) AND " +
            "(:type IS NULL OR a.type = :type) AND " +
            "(:startDate IS NULL OR a.dueAt >= :startDate) AND " +
            "(:endDate IS NULL OR a.dueAt <= :endDate) AND " +
            "(:leadId IS NULL OR a.leadId = :leadId) AND " +
            "(:opportunityId IS NULL OR a.opportunityId = :opportunityId) AND " +
            "(:accountId IS NULL OR a.accountId = :accountId) AND " +
            "(:contactId IS NULL OR a.contactId = :contactId)")
    Page<Activity> search(@Param("completed") Boolean completed,
                          @Param("type") ActivityType type,
                          @Param("startDate") LocalDateTime startDate,
                          @Param("endDate") LocalDateTime endDate,
                          @Param("leadId") Long leadId,
                          @Param("opportunityId") Long opportunityId,
                          @Param("accountId") Long accountId,
                          @Param("contactId") Long contactId,
                          Pageable pageable);
}
