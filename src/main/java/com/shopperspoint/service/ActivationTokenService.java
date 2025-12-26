package com.shopperspoint.service;

import com.shopperspoint.entity.ActivationToken;
import com.shopperspoint.exceptionhandler.InvalidTokenException;
import com.shopperspoint.exceptionhandler.TokenExpiredException;
import com.shopperspoint.repository.ActivationTokenRepo;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
public class ActivationTokenService {


    private final ActivationTokenRepo activationTokenRepo;

    @Autowired
    public ActivationTokenService(ActivationTokenRepo activationTokenRepo) {
        this.activationTokenRepo = activationTokenRepo;
    }

    @Value("${activation.token.time}")
    private int time;

    public String generateToken(String email) {
        String token = UUID.randomUUID().toString();
        LocalDateTime expiryTime = LocalDateTime.now().plusHours(time);

        ActivationToken activationToken = new ActivationToken(token, email, expiryTime);
        activationTokenRepo.save(activationToken);

        log.info("Generated token [{}] for email [{}] with expiry at [{}]", token, email, expiryTime);
        return token;
    }


    public String validateToken(String token) {
        Optional<ActivationToken> activationToken = activationTokenRepo.findByToken(token);

        if (activationToken.isEmpty()) {
            log.warn("Attempt to validate invalid token [{}]", token);
            throw new InvalidTokenException("Token is not valid ");
        }

        ActivationToken activeToken = activationToken.get();

        if (activeToken.getExpiryTime().isBefore(LocalDateTime.now())) {
            log.warn("Token [{}] expired at [{}]", token, activeToken.getExpiryTime());
            throw new TokenExpiredException("Token is expired");
        }

        log.info("Token [{}] validated successfully for email [{}]", token, activeToken.getEmail());
        return activeToken.getEmail();
    }


    public void deleteOldToken(String email) {
        Optional<ActivationToken> activationToken = activationTokenRepo.findByEmail(email);

        activationToken.ifPresent(token -> {
            activationTokenRepo.deleteByToken(token.getToken());
            log.info("Deleted old token [{}] for email [{}]", token.getToken(), email);
        });
    }

    @Scheduled(cron = "0 0 0 * * *")
    @Transactional
    public void deleteExpiredToken() {
        log.info("Scheduled job started to delete expired tokens");
        activationTokenRepo.deleteTokeByExpiryTime(LocalDateTime.now());
        log.info("Expired tokens deleted successfully");
    }

}
