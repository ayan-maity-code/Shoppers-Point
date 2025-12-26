package com.shopperspoint.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.shopperspoint.auditing.EntitiesAuditing;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@SQLDelete(sql = "update address set deleted = true where id=?")
@Where(clause = "deleted=false")
@Table(name = "address")
public class Address extends EntitiesAuditing {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    @Column(nullable = false)
    private String addressLine;
    @Column(nullable = false)
    private String label;                   // ex: "Home", "Office"
    @Column(nullable = false)
    private String city;
    @Column(nullable = false)
    private String state;
    @Column(nullable = false)
    private String country;
    @Column(nullable = false)
    private String zipCode;
    @Column(nullable = false)
    private Boolean deleted = false;


    @ManyToOne
    @JoinColumn(name = "user_id")
    @JsonBackReference
    private User user;


}
