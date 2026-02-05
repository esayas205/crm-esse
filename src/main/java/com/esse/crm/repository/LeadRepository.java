package com.esse.crm.repository;

import com.esse.crm.dto.lead.LeadSource;
import com.esse.crm.dto.lead.LeadStatus;
import com.esse.crm.entity.Lead;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface LeadRepository extends JpaRepository<Lead, Long> {

    @Query("SELECT l FROM Lead l WHERE " +
            "(:status IS NULL OR l.status = :status) AND " +
            "(:ownerUser IS NULL OR l.ownerUser = :ownerUser) AND " +
            "(:source IS NULL OR l.source = :source) AND " +
            "(:searchTerm IS NULL OR LOWER(l.company) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(l.contactName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(l.email) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(l.phone) LIKE LOWER(CONCAT('%', :searchTerm, '%')))")
    Page<Lead> search(@Param("status") LeadStatus status,
                      @Param("ownerUser") String ownerUser,
                      @Param("source") LeadSource source,
                      @Param("searchTerm") String searchTerm,
                      Pageable pageable);

    Optional<Lead> findByEmail(String email);
}
