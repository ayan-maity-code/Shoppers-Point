package com.shopperspoint.entity;


import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrimaryKeyJoinColumn;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
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
public class Customer extends User {

//    private String memberType; // like prime, vip

    @NotBlank(message = "Phone number cannot empty")
    @Pattern(regexp = "^[0-9]{10}$", message = "Phone number must be 10 digits and cannot start with 0")
    @NotNull
    private String phoneNumber;

    @OneToMany(mappedBy = "customer", cascade = {CascadeType.MERGE, CascadeType.PERSIST})
    private List<ProductReview> productReviews;

    @OneToMany(mappedBy = "customer", cascade = CascadeType.ALL)
    private List<Cart> cartItems;

    @OneToMany(mappedBy = "customer", cascade = CascadeType.ALL)
    private List<Order> orders;


}
