package com.shopperspoint.entity;

import com.shopperspoint.auditing.EntitiesAuditing;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "reset_password_token")
public class ResetPasswordToken extends EntitiesAuditing {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;


    @Column(nullable = false)
    private String token;

    @Column(nullable = false)
    private String email;


    @Column(nullable = false)
    private LocalDateTime expiryTime;


    public ResetPasswordToken(String token, String email, LocalDateTime expiryTime) {
        this.email = email;
        this.token = token;
        this.expiryTime = expiryTime;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;
}
