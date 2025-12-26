package com.shopperspoint.repository;

import com.shopperspoint.entity.ResetPasswordToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

public interface PasswordTokenRepo extends JpaRepository<ResetPasswordToken, Long> {

    Optional<ResetPasswordToken> findByToken(String token);

    Optional<ResetPasswordToken> findByEmail(String email);

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    void deleteByToken(String token);

    @Modifying
    @Transactional
    @Query("delete from  ActivationToken a where a.expiryTime < :currentTime ")
    void deleteTokeByExpiryTime(@Param("currentTime") LocalDateTime currentTime);

    Boolean existsByEmail(String email);

}
