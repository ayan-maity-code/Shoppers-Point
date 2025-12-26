package com.shopperspoint.security;

import com.shopperspoint.jwt.JwtAuthenticationEntryPoint;
import com.shopperspoint.jwt.JwtFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfig {


    private final CustomAuthenticationProvider authenticationProvider;
    private final JwtFilter jwtFilter;
    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

    @Autowired
    public SecurityConfig(CustomAuthenticationProvider authenticationProvider, JwtFilter jwtFilter, JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint) {
        this.authenticationProvider = authenticationProvider;
        this.jwtFilter = jwtFilter;
        this.jwtAuthenticationEntryPoint = jwtAuthenticationEntryPoint;
    }


    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(authorizeRequest -> authorizeRequest
                        .requestMatchers("/api/account/register/customer",
                                "/api/account/activate",
                                "/api/account/resendActivation",
                                "/api/user/**",
                                "/api/vendor/register/seller").permitAll()
                        .requestMatchers("/api/management/**").hasRole("ADMIN")
                        .requestMatchers("/api/vendor/**").hasRole("SELLER")
                        .requestMatchers("/api/account/**").hasRole("CUSTOMER")
                        .anyRequest().authenticated()
                )
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
                .exceptionHandling(exception ->
                        exception.authenticationEntryPoint(jwtAuthenticationEntryPoint));


        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return new ProviderManager(List.of(authenticationProvider));
    }


}
