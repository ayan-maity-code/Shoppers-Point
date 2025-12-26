package com.shopperspoint.entity;


import com.shopperspoint.auditing.EntitiesAuditing;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import java.util.List;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@SQLDelete(sql = "update product set is_deleted = true where id=?")
@Where(clause = "is_deleted=false")
public class Product extends EntitiesAuditing {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    private String name;
    private String description;
    private String brand;

    private Boolean isCancellable;
    private Boolean isReturnable;
    private Boolean isActive;

    @Column(name = "is_deleted")
    private Boolean isDeleted;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL)
    private List<ProductVariation> productVariations;

    @OneToMany(mappedBy = "product", cascade = {CascadeType.MERGE, CascadeType.PERSIST})
    private List<ProductReview> reviews;

    @ManyToOne
    @JoinColumn(name = "seller_id")
    private Seller seller;

    @ManyToOne
    @JoinColumn(name = "category_id")
    private Category category;


}
