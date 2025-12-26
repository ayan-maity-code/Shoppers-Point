package com.shopperspoint.service;


import com.shopperspoint.dto.GenericResponse;
import com.shopperspoint.dto.UserPrinciple;
import com.shopperspoint.email.EmailService;
import com.shopperspoint.entity.User;
import com.shopperspoint.exceptionhandler.BadRequestException;
import com.shopperspoint.exceptionhandler.TokenExpiredException;
import com.shopperspoint.exceptionhandler.UserNotFoundException;
import com.shopperspoint.jwt.JwtUtil;
import com.shopperspoint.repository.UserRepo;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Locale;

@Service
@Slf4j
public class LoginService {
    private final UserRepo userRepo;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final MessageSource messageSource;

    @Autowired
    public LoginService(UserRepo userRepo, PasswordEncoder passwordEncoder, EmailService emailService,
                        AuthenticationManager authenticationManager, JwtUtil jwtUtil, MessageSource messageSource) {
        this.userRepo = userRepo;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
        this.messageSource = messageSource;
    }

    @Value("${refresh.expiration}")
    private int expiration;

    @Value("${password.expired.day}")
    private int day;

    @Value("${success.message}")
    private String message;

    @Transactional(noRollbackFor = {TokenExpiredException.class, UserNotFoundException.class})
    public ResponseEntity<GenericResponse> userLogin(String email, String password, HttpServletResponse response, Locale locale) {
        log.info("Attempting login for user with email: {}", email);

        User user = userRepo.findByEmail(email)
                .orElseThrow(() -> {
                    log.error("User with email {} not found", email);
                    return new UserNotFoundException("User with email " + email + " not found!");
                });

        if (Boolean.TRUE.equals(user.getIsExpired())) {
            log.error("Password expired for user with email {}", email);
            throw new BadRequestException("Password expired");
        }

        if (user.getPasswordUpdatedDate().plusDays(day).isBefore(LocalDateTime.now())) {
            user.setIsExpired(true);
            log.error("Password expired for user with email {}", email);
            throw new BadRequestException("Password expired, please reset you password");
        }


        if (!passwordEncoder.matches(password, user.getPassword())) {
            if (Boolean.TRUE.equals(user.getIsLocked())) {
                log.error("Account is locked for user with email {}", email);
                throw new BadRequestException("Account is locked");
            }

            int newAttempt = user.getInvalidAttemptCount() + 1;
            user.setInvalidAttemptCount(newAttempt);

            if (newAttempt >= 3) {
                emailService.sendEmailForAccountLocked(email);
                user.setIsLocked(true);
                log.error("Account locked due to multiple invalid attempts for user with email {}", email);
                throw new TokenExpiredException("Account has been locked due to multiple invalid attempt");
            }

            userRepo.save(user);

            log.warn("Invalid password attempt #{} for user with email {}", newAttempt, email);
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(new GenericResponse("Invalid password.You have " + (3 - newAttempt) + " attempt left before your account gets locked", "INVALID_CREDENTIALS", LocalDateTime.now()));

        }


        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(email, password)
        );

        UserPrinciple userPrinciple = (UserPrinciple) authentication.getPrincipal();


        if (!userPrinciple.isAccountNonLocked()) {
            log.error("Account is locked for user with email {}", email);
            throw new BadRequestException("You account is locked");
        }


        if (user.getInvalidAttemptCount() > 0 && user.getInvalidAttemptCount() < 3) {
            user.setInvalidAttemptCount(0);
            userRepo.save(user);
            log.info("Invalid attempt count reset for user with email {}", email);
        }

        if (userPrinciple.isEnabled()) {
            throw new BadRequestException("Account is not active");
        }


        String access = jwtUtil.generateToken(email, "access");

        String refresh = jwtUtil.generateToken(email, "refresh");

        Cookie accessTokenCookie = new Cookie("accessToken", access);
        accessTokenCookie.setHttpOnly(true);
        accessTokenCookie.setPath("/");
        accessTokenCookie.setMaxAge(expiration);

        Cookie refreshTokenCookie = new Cookie("refreshToken", refresh);
        refreshTokenCookie.setHttpOnly(true);
        refreshTokenCookie.setPath("/");
        refreshTokenCookie.setMaxAge(expiration);

        response.addCookie(accessTokenCookie);
        response.addCookie(refreshTokenCookie);

        log.info("Login successful for user with email {}", email);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(new GenericResponse(messageSource.getMessage("message.greetings", null, locale) + access, message, LocalDateTime.now()));


    }


}
