package com.shopperspoint.entity;

import com.shopperspoint.auditing.EntitiesAuditing;
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
public class ProductVariation extends EntitiesAuditing {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;


    @Column(columnDefinition = "json")
    private String metaData;

    @Column(nullable = false)
    private Integer quantityAvailable;
    @Column(nullable = false)
    private Long price;
    private String primaryImageName;

    private Boolean isActive;

    @ManyToOne
    @JoinColumn(name = "product_id")
    private Product product;

    @OneToMany(mappedBy = "productVariation", cascade = CascadeType.ALL)
    private List<OrderProduct> orderProducts;

    @OneToMany(mappedBy = "productVariation", cascade = CascadeType.ALL)
    private List<Cart> carts;


}
