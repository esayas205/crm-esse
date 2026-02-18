package com.esse.crm.security.service;

import com.esse.crm.security.entity.AppUser;
import com.esse.crm.security.entity.RefreshToken;
import com.esse.crm.security.repository.RefreshTokenRepository;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Base64;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class RefreshTokenService {

    @Value("${application.security.jwt.refresh-token.expiration}")
    private long refreshTokenExpiration;

    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;

    @Getter
    @Builder
    public static class TokenCreationResult {
        private RefreshToken refreshToken;
        private String rawToken;
    }

    @Transactional
    public TokenCreationResult generateRefreshToken(AppUser user, String deviceInfo, String ipAddress, String family) {
        String tokenId = UUID.randomUUID().toString();
        String secret = UUID.randomUUID().toString();
        String tokenFamily = (family == null) ? UUID.randomUUID().toString() : family;

        RefreshToken refreshToken = RefreshToken.builder()
                .tokenId(tokenId)
                .user(user)
                .tokenHash(passwordEncoder.encode(secret))
                .tokenFamily(tokenFamily)
                .expiresAt(LocalDateTime.now().plusNanos(refreshTokenExpiration * 1_000_000))
                .createdAt(LocalDateTime.now())
                .deviceInfo(deviceInfo)
                .ipAddress(ipAddress)
                .build();

        refreshTokenRepository.save(refreshToken);
        String rawToken = Base64.getEncoder().encodeToString((tokenId + ":" + secret).getBytes());

        return TokenCreationResult.builder()
                .refreshToken(refreshToken)
                .rawToken(rawToken)
                .build();
    }

    @Transactional
    public TokenCreationResult rotateToken(String rawRefreshToken) {
        String decoded;
        try {
            decoded = new String(Base64.getDecoder().decode(rawRefreshToken));
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Invalid refresh token format");
        }
        
        String[] parts = decoded.split(":");
        if (parts.length != 2) {
            throw new RuntimeException("Invalid refresh token structure");
        }
        String tokenId = parts[0];
        String secret = parts[1];

        RefreshToken token = refreshTokenRepository.findByTokenId(tokenId)
                .orElseThrow(() -> new RuntimeException("Refresh token not found"));

        if (token.isRevoked() || token.isExpired()) {
            log.warn("Refresh token is revoked or expired. Revoking token family: {}", token.getTokenFamily());
            revokeTokenFamily(token.getTokenFamily());
            throw new RuntimeException("Refresh token is invalid (revoked or expired)");
        }

        if (!passwordEncoder.matches(secret, token.getTokenHash())) {
            revokeTokenFamily(token.getTokenFamily());
            throw new RuntimeException("Invalid refresh token secret (reuse detected)");
        }

        token.setRevokedAt(LocalDateTime.now());
        token.setLastUsedAt(LocalDateTime.now());
        refreshTokenRepository.save(token);

        return generateRefreshToken(token.getUser(), token.getDeviceInfo(), token.getIpAddress(), token.getTokenFamily());
    }

    private void revokeTokenFamily(String family) {
        List<RefreshToken> familyTokens = refreshTokenRepository.findByTokenFamily(family);
        familyTokens.forEach(t -> {
            if (t.getRevokedAt() == null) {
                t.setRevokedAt(LocalDateTime.now());
            }
        });
        refreshTokenRepository.saveAll(familyTokens);
    }
    
    @Transactional
    public void revokeToken(String rawRefreshToken) {
        String decoded;
        try {
            decoded = new String(Base64.getDecoder().decode(rawRefreshToken));
        } catch (IllegalArgumentException e) {
            return; // Ignore invalid token
        }
        
        String[] parts = decoded.split(":");
        if (parts.length != 2) return;
        
        String tokenId = parts[0];
        refreshTokenRepository.findByTokenId(tokenId).ifPresent(token -> {
            token.setRevokedAt(LocalDateTime.now());
            refreshTokenRepository.save(token);
        });
    }

    @Transactional
    public void revokeAllUserTokens(Long userId) {
        refreshTokenRepository.deleteByUserId(userId);
    }
}
