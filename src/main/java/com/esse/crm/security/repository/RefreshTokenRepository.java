package com.esse.crm.security.repository;

import com.esse.crm.security.entity.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    Optional<RefreshToken> findByTokenId(String tokenId);
    List<RefreshToken> findByTokenFamily(String tokenFamily);
    void deleteByUserId(Long userId);
}
