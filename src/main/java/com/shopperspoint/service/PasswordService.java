package com.shopperspoint.service;


import com.shopperspoint.dto.GenericResponse;
import com.shopperspoint.dto.PasswordDTO;
import com.shopperspoint.email.EmailService;
import com.shopperspoint.entity.User;
import com.shopperspoint.exceptionhandler.BadRequestException;
import com.shopperspoint.exceptionhandler.UserNotFoundException;
import com.shopperspoint.repository.PasswordTokenRepo;
import com.shopperspoint.repository.UserRepo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@Slf4j
public class PasswordService {

    private final PasswordTokenRepo passwordTokenRepo;
    private final UserRepo userRepo;
    private final ResetPasswordTokenService tokenService;
    private final EmailService emailService;
    private final PasswordEncoder encoder;

    @Autowired
    public PasswordService(PasswordTokenRepo passwordTokenRepo,
                           UserRepo userRepo,
                           ResetPasswordTokenService tokenService,
                           EmailService emailService,
                           PasswordEncoder encoder) {
        this.passwordTokenRepo = passwordTokenRepo;
        this.userRepo = userRepo;
        this.tokenService = tokenService;
        this.emailService = emailService;
        this.encoder = encoder;
    }


    @Value("${app.regex.email}")
    private String emailRegex;

    @Value("${success.message}")
    private String message;


    @Transactional
    public ResponseEntity<GenericResponse> forgotPassword(String email) {
        log.info("Request received for forgot password for email: {}", email);
        if (!email.matches(emailRegex)) {
            log.error("Invalid email format: {}", email);
            throw new BadRequestException("Invalid email format");

        }

        User user = userRepo.findByEmail(email).orElse(null);

        if (user == null) {
            log.error("User not found for email: {}", email);
            throw new UserNotFoundException("User not found");
        }

        if (Boolean.FALSE.equals(user.getIsActive())) {
            log.warn("User is not active: {}", email);
            throw new UserNotFoundException("User is not active");
        }

        if (Boolean.TRUE.equals(user.getIsLocked())) {
            log.warn("User account is locked: {}", email);
            throw new UserNotFoundException("Your account is locked, kindly contact to admin");
        }

        tokenService.deleteOldToken(email);

        String token = tokenService.generateToken(email);

        emailService.sendEmailForForgotPassword(user.getEmail(), token);
        log.info("Password reset email sent for user: {}", email);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(new GenericResponse("Successfully sent the link, Please check your email to reset password   " + token, message, LocalDateTime.now()));

    }


    @Transactional
    public ResponseEntity<GenericResponse> resetPassword(String token, PasswordDTO passwordDTO) {
        log.info("Request received to reset password using token: {}", token);
        String email = tokenService.validateToken(token);


        User user = userRepo.findByEmail(email).orElse(null);

        if (user == null) {
            log.error("User not found for email: {}", email);
            throw new UserNotFoundException("User not found.");

        }

        log.info("Resetting password for user: {}", email);

        user.setPassword(encoder.encode(passwordDTO.getPassword()));
        user.setInvalidAttemptCount(0);
        user.setIsLocked(false);
        user.setPasswordUpdatedDate(LocalDateTime.now());
        user.setIsExpired(false);
        userRepo.save(user);

        tokenService.deleteOldToken(email);
        log.info("Password successfully reset for user: {}", email);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(new GenericResponse("Password changed successfully", message, LocalDateTime.now()));

    }

}
