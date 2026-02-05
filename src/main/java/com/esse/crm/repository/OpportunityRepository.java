package com.esse.crm.repository;

import com.esse.crm.dto.opportunity.OpportunityStage;
import com.esse.crm.entity.Opportunity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;

@Repository
public interface OpportunityRepository extends JpaRepository<Opportunity, Long> {

    @Query("SELECT o FROM Opportunity o WHERE " +
            "(:stage IS NULL OR o.stage = :stage) AND " +
            "(:accountId IS NULL OR o.account.id = :accountId) AND " +
            "(:startDate IS NULL OR o.closeDate >= :startDate) AND " +
            "(:endDate IS NULL OR o.closeDate <= :endDate) AND " +
            "(:minAmount IS NULL OR o.amount >= :minAmount) AND " +
            "(:maxAmount IS NULL OR o.amount <= :maxAmount)")
    Page<Opportunity> search(@Param("stage") OpportunityStage stage,
                             @Param("accountId") Long accountId,
                             @Param("startDate") LocalDate startDate,
                             @Param("endDate") LocalDate endDate,
                             @Param("minAmount") BigDecimal minAmount,
                             @Param("maxAmount") BigDecimal maxAmount,
                             Pageable pageable);
}
