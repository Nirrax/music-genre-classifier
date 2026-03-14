package com.sa.spring_api.auth.repository;

import com.sa.spring_api.auth.entity.RefreshToken;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    Optional<RefreshToken> findByToken(String token);

    @Modifying
    @Transactional
    int deleteByExpiresAtBefore(Instant expiryTime);

    @Modifying
    void deleteRefreshTokenByUserId(UUID userId);
}
