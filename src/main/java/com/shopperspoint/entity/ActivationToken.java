package com.shopperspoint.entity;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class ActivationToken {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String token;

    @Column(nullable = false)
    private String email;


    @Column(nullable = false)
    private LocalDateTime expiryTime;


    public ActivationToken(String token, String email, LocalDateTime expiryTime) {
        this.email = email;
        this.token = token;
        this.expiryTime = expiryTime;
    }
}
