package com.shopperspoint.entity;


import com.shopperspoint.key.ProductReviewKey;
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
public class ProductReview {

    @EmbeddedId
    private ProductReviewKey id;

    private String review;
    private String rating;

    @ManyToOne
    @MapsId("productId")
    @JoinColumn(name = "product_id")
    private Product product;

    @ManyToOne
    @MapsId("customerUserId")
    @JoinColumn(name = "customer_user_id")
    private Customer customer;

}
