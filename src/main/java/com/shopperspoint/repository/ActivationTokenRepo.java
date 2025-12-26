package com.shopperspoint.repository;

import com.shopperspoint.entity.ActivationToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public interface ActivationTokenRepo extends JpaRepository<ActivationToken, Long> {

    Optional<ActivationToken> findByToken(String token);

    Optional<ActivationToken> findByEmail(String email);

    void deleteByToken(String token);

    @Modifying
    @Transactional
    @Query("delete from  ActivationToken a where a.expiryTime < :currentTime ")
    void deleteTokeByExpiryTime(@Param("currentTime") LocalDateTime currentTime);
}
