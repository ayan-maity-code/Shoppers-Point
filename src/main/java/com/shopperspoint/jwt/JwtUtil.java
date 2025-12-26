package com.shopperspoint.jwt;


import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Service
public class JwtUtil {

    private final SecretKey key;

    @Value("${refresh.expiration.time}")
    private int refreshExpirationTime;

    @Value("${access.expiration.time}")
    private int accessExpirationTime;


    public JwtUtil(@Value("${jwt.secret}") String secretKey) {
        byte[] decodedKey = Decoders.BASE64.decode(secretKey);
        this.key = Keys.hmacShaKeyFor(decodedKey);

    }

    public String generateToken(String email, String type) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("token_type", type);

        long expiryTimeMillis = "access".equals(type) ? accessExpirationTime : refreshExpirationTime;

        return  Jwts.builder()
                .setClaims(claims)
                .setSubject(email)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + expiryTimeMillis))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }


    private SecretKey getKey() {
        return key;
    }


    public String extractUserName(String token) {
        // extract the username from jwt token
        return extractClaim(token, Claims::getSubject);
    }

    private <T> T extractClaim(String token, Function<Claims, T> claimResolver) {
        final Claims claims = extractAllClaims(token);
        return claimResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public boolean validateToken(String token, UserDetails userDetails) {
        final String userName = extractUserName(token);
        return (userName.equals(userDetails.getUsername()) && !isTokenExpired(token));
    }

    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    public String extractTokenType(String token) {
        return extractAllClaims(token).get("token_type", String.class);
    }
}
