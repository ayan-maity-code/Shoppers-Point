package com.shopperspoint.repository;

import com.shopperspoint.entity.Seller;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface SellerRepo extends JpaRepository<Seller, Long> {

    @Query(
            "select s from Seller s join s.roles r " +
                    "where r.authority = :roleName " +
                    "and (:email is null or s.email like %:email%)"
    )
    List<Seller> findByRoleAndEmailFilter(
            @Param("roleName") String roleName,
            @Param("email") String email,
            Pageable pageable
    );

    Seller findByEmail(String email);

    Boolean existsByGst(String gst);
}
