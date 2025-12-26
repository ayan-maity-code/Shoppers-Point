package com.shopperspoint.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@PrimaryKeyJoinColumn(name = "id")
public class Seller extends User {

    @Column(unique = true, nullable = false)
    private String gst;

    @Column(nullable = false)
    private String companyContact;

    @Column(unique = true, nullable = false)
    private String companyName;


    @OneToMany(mappedBy = "seller", cascade = CascadeType.ALL)
    private List<Product> products;

}
