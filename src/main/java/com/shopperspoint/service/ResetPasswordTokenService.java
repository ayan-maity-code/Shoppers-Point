package com.shopperspoint.service;

import com.shopperspoint.entity.ResetPasswordToken;
import com.shopperspoint.entity.User;
import com.shopperspoint.exceptionhandler.InvalidTokenException;
import com.shopperspoint.exceptionhandler.TokenExpiredException;
import com.shopperspoint.repository.PasswordTokenRepo;
import com.shopperspoint.repository.UserRepo;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
@Slf4j
public class ResetPasswordTokenService {

    private final PasswordTokenRepo passwordTokenRepo;
    private final UserRepo userRepo;

    @Autowired
    public ResetPasswordTokenService(PasswordTokenRepo passwordTokenRepo, UserRepo userRepo) {
        this.passwordTokenRepo = passwordTokenRepo;
        this.userRepo = userRepo;
    }


    @Value("${reset.password.token.time}")
    private int time;

    public String generateToken(String email) {
        log.info("Generating reset password token for email: {}", email);
        LocalDateTime expiryTime = LocalDateTime.now().plusMinutes(time);
        String token = UUID.randomUUID().toString();
        ResetPasswordToken resetPasswordToken = new ResetPasswordToken(token, email, expiryTime);
        User user = userRepo.findByEmail(email).get();
        resetPasswordToken.setUser(user);
        passwordTokenRepo.save(resetPasswordToken);
        log.info("Generated token: {} with expiry at: {}", token, expiryTime);
        return token;
    }

    public String validateToken(String token) {
        log.info("Validating reset password token: {}", token);
        Optional<ResetPasswordToken> passwordToken = passwordTokenRepo.findByToken(token);

        if (passwordToken.isEmpty()) {
            log.error("Invalid token provided: {}", token);
            throw new InvalidTokenException("Token is not valid, password not updated");
        }

        ResetPasswordToken resetPasswordToken = passwordToken.get();

        if (resetPasswordToken.getExpiryTime().isBefore(LocalDateTime.now())) {
            passwordTokenRepo.deleteByToken(token);
            log.warn("Expired token attempted: {}", token);
            throw new TokenExpiredException("Token is expired, password not updated");
        }
        log.info("Token validated successfully for email: {}", resetPasswordToken.getEmail());
        return resetPasswordToken.getEmail();
    }

    public void deleteOldToken(String email) {
        log.info("Deleting old reset token for email: {}", email);
        Optional<ResetPasswordToken> passwordToken = passwordTokenRepo.findByEmail(email);

        passwordToken.ifPresent(
                token -> {
                    passwordTokenRepo.deleteByToken(token.getToken());
                    log.info("Old token deleted: {}", token.getToken());
                });
    }

    @Scheduled(cron = "0 0 0 * * *")
    @Transactional
    public void deleteExpiredToken() {
        log.info("Scheduled cleanup of expired tokens started at: {}", LocalDateTime.now());
        passwordTokenRepo.deleteTokeByExpiryTime(LocalDateTime.now());
        log.info("Scheduled cleanup of expired tokens completed.");
    }
}
