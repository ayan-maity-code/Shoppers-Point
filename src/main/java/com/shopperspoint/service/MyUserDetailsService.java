package com.shopperspoint.service;

import com.shopperspoint.dto.UserPrinciple;
import com.shopperspoint.entity.User;
import com.shopperspoint.repository.UserRepo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class MyUserDetailsService implements UserDetailsService {
    private final UserRepo userRepo;

    @Autowired
    public MyUserDetailsService(UserRepo userRepo) {
        this.userRepo = userRepo;
    }


    @Override
    public UserDetails loadUserByUsername(String email) {
        log.info("Attempting to load user by email: {}", email);
        User user = userRepo.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Email not found"));
        log.info("User found: {}", user.getEmail());
        return new UserPrinciple(user);
    }

}
