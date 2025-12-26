package com.shopperspoint.repository;

import com.shopperspoint.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

public interface UserRepo extends JpaRepository<User, Long> {
    Boolean existsByEmail(String email);

    Optional<User> findByEmail(String email);


    @Query("Select count(s) > 0 from Seller s where lower(s.companyName) = lower(:companyName)")
    Boolean existByCompanyNameIgnoreCase(@Param("companyName") String companyName);

    @Modifying
    @Transactional
    @Query("update User u set u.invalidAttemptCount= ?1 where u.email= ?2 ")
    void updateInvalidAttemptCount(int attempt, String email);


}
