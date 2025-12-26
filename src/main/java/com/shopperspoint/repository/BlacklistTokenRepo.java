package com.shopperspoint.repository;

import com.shopperspoint.entity.BlacklistedToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public interface BlacklistTokenRepo extends JpaRepository<BlacklistedToken, Long> {

    Boolean existsByAccessToken(String accessToken);

    Boolean existsByRefreshToken(String refreshToken);

    void deleteByBlackListedAtBefore(LocalDateTime expirationTime);

}
