package com.esse.crm.repository;

import com.esse.crm.entity.Account;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AccountRepository extends JpaRepository<Account, Long> {
    Optional<Account> findByAccountName(String accountName);

    @Query("SELECT a FROM Account a WHERE " +
           "(:searchTerm IS NULL OR LOWER(a.accountName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(a.industry) LIKE LOWER(CONCAT('%', :searchTerm, '%')))")
    Page<Account> searchAccounts(@Param("searchTerm") String searchTerm, Pageable pageable);
}
