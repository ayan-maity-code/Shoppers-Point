package com.shopperspoint.jwt;

import com.shopperspoint.exceptionhandler.InvalidTokenException;
import com.shopperspoint.repository.BlacklistTokenRepo;
import com.shopperspoint.service.MyUserDetailsService;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.file.AccessDeniedException;

@Component
public class JwtFilter extends OncePerRequestFilter {
    private final JwtUtil jwtUtil;
    private final ApplicationContext context;
    private final BlacklistTokenRepo blacklistTokenRepo;

    @Autowired
    public JwtFilter(JwtUtil jwtUtil, ApplicationContext context, BlacklistTokenRepo blacklistTokenRepo) {
        this.jwtUtil = jwtUtil;
        this.context = context;
        this.blacklistTokenRepo = blacklistTokenRepo;
    }


    @Value("${refresh.expiration}")
    private int expiration;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        if (request.getRequestURI().contains("/api/public")) {
            filterChain.doFilter(request, response);
            return;
        }

        String accessToken = getCookieValue(request, "accessToken");
        String refreshToken = getCookieValue(request, "refreshToken");
        try {
            if (accessToken != null) {
                if (blacklistTokenRepo.existsByAccessToken(accessToken)) {
                    throw new AccessDeniedException("Access token  blacklisted");
                }


                String userName = jwtUtil.extractUserName(accessToken);

                if (jwtUtil.extractTokenType(accessToken).equals("refresh")) {
                    throw new AccessDeniedException("Refresh tokens are not allowed to access this resource.");
                }

                if (userName != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                    UserDetails userDetails = context.getBean(MyUserDetailsService.class).loadUserByUsername(userName);
                    if (jwtUtil.validateToken(accessToken, userDetails)) {
                        setAuthentication(userDetails, request);
                    }
                }
            }

        } catch (ExpiredJwtException e) {
            if (refreshToken != null) {
                try {
                    if (blacklistTokenRepo.existsByRefreshToken(refreshToken)) {
                        throw new AccessDeniedException("Refresh token is blacklisted");
                    }

                    String username = jwtUtil.extractUserName(refreshToken);
                    if ("refresh".equals(jwtUtil.extractTokenType(refreshToken))) {
                        String newAccessToken = jwtUtil.generateToken(username, "access");
                        Cookie newAccessTokenCookie = new Cookie("accessToken", newAccessToken);
                        newAccessTokenCookie.setHttpOnly(true);
                        newAccessTokenCookie.setPath("/");
                        newAccessTokenCookie.setMaxAge(expiration);
                        response.addCookie(newAccessTokenCookie);
                        UserDetails userDetails = context.getBean(MyUserDetailsService.class).loadUserByUsername(username);
                        setAuthentication(userDetails, request);
                    }
                } catch (JwtException ex) {
                    request.setAttribute("exception", "Invalid refresh token");
                }
            }
        } catch (JwtException e) {
            request.setAttribute("exception", "Invalid access token");

        } catch (AccessDeniedException e) {
            request.setAttribute("exception", e.getMessage());
        }

        filterChain.doFilter(request, response);
    }

    private String getCookieValue(HttpServletRequest request, String name) {
        try {
            if (request.getCookies() != null) {
                for (Cookie cookie : request.getCookies()) {
                    if (name.equals(cookie.getName())) {
                        return cookie.getValue();
                    }
                }
            }
            throw new InvalidTokenException("Could not get token form cookie");
        } catch (InvalidTokenException e) {
            request.setAttribute("exception", e.getMessage());
        }
        return null;
    }

    private void setAuthentication(UserDetails userDetails, HttpServletRequest request) {
        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                userDetails, null, userDetails.getAuthorities()
        );

        authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        SecurityContextHolder.getContext().setAuthentication(authToken);
    }
}
