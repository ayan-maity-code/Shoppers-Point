package com.shopperspoint.service;

import com.shopperspoint.repository.BlacklistTokenRepo;
import jakarta.transaction.Transactional;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class BlackListedTokenService {

    private final BlacklistTokenRepo blacklistedTokenRepository;

    public BlackListedTokenService(BlacklistTokenRepo blacklistedTokenRepository) {
        this.blacklistedTokenRepository = blacklistedTokenRepository;
    }

    @Scheduled(cron = "0 0 0 * * *")
    @Transactional
    public void cleanExpiredBlacklistedToken() {
        blacklistedTokenRepository.deleteByBlackListedAtBefore(LocalDateTime.now());
    }
}
