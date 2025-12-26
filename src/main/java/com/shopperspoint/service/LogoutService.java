package com.shopperspoint.service;


import com.shopperspoint.dto.GenericResponse;
import com.shopperspoint.entity.BlacklistedToken;
import com.shopperspoint.entity.User;
import com.shopperspoint.exceptionhandler.InvalidTokenException;
import com.shopperspoint.exceptionhandler.UserNotFoundException;
import com.shopperspoint.jwt.JwtUtil;
import com.shopperspoint.repository.BlacklistTokenRepo;
import com.shopperspoint.repository.UserRepo;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Locale;

@Service
@Slf4j
public class LogoutService {
    private final BlacklistTokenRepo blacklistTokenRepo;
    private final UserRepo userRepo;
    private final JwtUtil jwtUtil;
    private final MessageSource messageSource;

    @Autowired
    public LogoutService(BlacklistTokenRepo blacklistTokenRepo, UserRepo userRepo, JwtUtil jwtUtil, MessageSource messageSource) {
        this.blacklistTokenRepo = blacklistTokenRepo;
        this.userRepo = userRepo;
        this.jwtUtil = jwtUtil;
        this.messageSource = messageSource;
    }

    @Value("${age.cookie}")
    private int age;

    @Value("${success.message}")
    private String message;

    public ResponseEntity<GenericResponse> logout(HttpServletRequest request, HttpServletResponse response, Locale locale) {
        String accessToken = getCookieValue(request, "accessToken");
        String refreshToken = getCookieValue(request, "refreshToken");
        String email = jwtUtil.extractUserName(accessToken);
        User user = userRepo.findByEmail(jwtUtil.extractUserName(accessToken)).orElseThrow(
                () -> new UserNotFoundException("User not found")
        );

        log.info("User with email {} is attempting to log out", email);

        if (accessToken != null && !blacklistTokenRepo.existsByAccessToken(accessToken) &&
                refreshToken != null && !blacklistTokenRepo.existsByRefreshToken(refreshToken)) {
            blacklistTokenRepo.save(new BlacklistedToken(null, accessToken, refreshToken, LocalDateTime.now(), user));
            log.info("Tokens for user with email {} added to blacklist", email);
        }


        Cookie expiredAccess = new Cookie("accessToken", null);
        expiredAccess.setMaxAge(age);
        expiredAccess.setPath("/");

        Cookie expiredRefresh = new Cookie("refreshToken", null);
        expiredRefresh.setMaxAge(age);
        expiredRefresh.setPath("/");

        response.addCookie(expiredAccess);
        response.addCookie(expiredRefresh);

        log.info("User with email {} has successfully logged out", email);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(new GenericResponse(messageSource.getMessage("logout.success", null, locale), message, LocalDateTime.now()));

    }

    private String getCookieValue(HttpServletRequest request, String name) {
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if (name.equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        log.error("Token cookie {} not found", name);
        throw new InvalidTokenException("Could not get token form cookie");
    }
}
