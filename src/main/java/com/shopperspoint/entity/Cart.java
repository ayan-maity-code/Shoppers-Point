package com.shopperspoint.entity;


import com.shopperspoint.key.CartKey;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Cart {

    @EmbeddedId
    private CartKey id;

    @ManyToOne
    @MapsId("customerUserId")
    @JoinColumn(name = "CUSTOMER_USER_ID")
    private Customer customer;


    private String quality;

    private Boolean isWishListItem = false;

    @ManyToOne
    @MapsId("productVariationId")
    @JoinColumn(name = "product_variation_id")
    private ProductVariation productVariation;
}
