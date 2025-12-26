package com.shopperspoint.repository;

import com.shopperspoint.entity.Customer;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CustomerRepo extends JpaRepository<Customer, Long> {

    @Query(
            "select c from Customer c join c.roles r " +
                    "where r.authority = :roleName " +
                    "and (:email is null or c.email like %:email%)"
    )
    List<Customer> findByRoleAndEmailFilter(
            @Param("roleName") String roleName,
            @Param("email") String email,
            Pageable pageable
    );


    Customer findByEmail(String email);
}
