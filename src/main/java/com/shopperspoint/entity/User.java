package com.shopperspoint.entity;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.shopperspoint.auditing.EntitiesAuditing;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "app_user")
@Inheritance(strategy = InheritanceType.JOINED)
public class User extends EntitiesAuditing {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    @Column(nullable = false)
    private String firstName;

    private String middleName;
    private String lastName;


    @Column(unique = true, nullable = false)
    private String email;


    @Column(nullable = false, length = 255)
    private String password;


    private Boolean isDeleted;
    private Boolean isActive;
    private Boolean isExpired;
    private Boolean isLocked;
    private Integer invalidAttemptCount;

    private LocalDateTime passwordUpdatedDate;


    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    @JsonManagedReference
    private List<Address> addresses;

    @ManyToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinTable(
            name = "user_role",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    private Set<Role> roles;


    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    List<BlacklistedToken> blacklistedTokens;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    List<ResetPasswordToken> resetPasswordTokens;

}
