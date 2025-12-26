package com.shopperspoint.repository;

import com.shopperspoint.entity.Address;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;

@Service
public interface AddressRepo extends JpaRepository<Address, Long> {

}
