package com.shopperspoint.security;

import com.shopperspoint.exceptionhandler.PasswordNotMatchException;
import com.shopperspoint.repository.UserRepo;
import com.shopperspoint.service.MyUserDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;


@Component
public class CustomAuthenticationProvider implements AuthenticationProvider {

    private final MyUserDetailsService userDetailsService;
    private final PasswordEncoder encoder;
    private final UserRepo userRepo;

    @Autowired
    public CustomAuthenticationProvider(MyUserDetailsService userDetailsService, PasswordEncoder encoder, UserRepo userRepo) {
        this.userDetailsService = userDetailsService;
        this.encoder = encoder;
        this.userRepo = userRepo;
    }

    @Override
    @Transactional
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        String email = authentication.getName();
        String password = authentication.getCredentials().toString();

        UserDetails username = userDetailsService.loadUserByUsername(email);

        if (!encoder.matches(password, username.getPassword())) {

            throw new PasswordNotMatchException("Username or Password not match ");
        }

        return new UsernamePasswordAuthenticationToken(username, password);

    }

    @Override
    public boolean supports(Class<?> authentication) {
        return authentication.equals(UsernamePasswordAuthenticationToken.class);
    }
}
