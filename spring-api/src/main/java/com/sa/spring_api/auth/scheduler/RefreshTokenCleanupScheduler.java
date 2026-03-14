package com.sa.spring_api.auth.scheduler;

import com.sa.spring_api.auth.repository.RefreshTokenRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
public class RefreshTokenCleanupScheduler {
    private static final Logger log = LoggerFactory.getLogger(RefreshTokenCleanupScheduler.class);
    private final RefreshTokenRepository refreshTokenRepository;

    public RefreshTokenCleanupScheduler(RefreshTokenRepository refreshTokenRepository) {
        this.refreshTokenRepository = refreshTokenRepository;
    }

    @Scheduled(cron = "0 0 4 * * ?")
    public void cleanupExpiredTokens() {
        Instant now = Instant.now();
        int deletedCount = refreshTokenRepository.deleteByExpiresAtBefore(now);

        log.info("Deleted {} expired tokens", deletedCount);
    }
}
