package com.shopperspoint.repository;

import com.shopperspoint.entity.ProductVariation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ProductVariationRepo extends JpaRepository<ProductVariation, Long> {

    List<ProductVariation> findByProductIdIn(List<Long> productIds);

    List<ProductVariation> findByProductId(Long productId);

    List<ProductVariation> findByProductIdAndIsActiveTrue(Long productId);

    Page<ProductVariation> findByProductId(Long productId, Pageable pageable);

    @Query("""
            select pv from ProductVariation pv
            where pv.product.id = :productId
            and(
            :query is null or
            lower(pv.metaData) like lower(concat('%', :query, '%'))
            )
            """)
    Page<ProductVariation> findByProductIdAndMetaDataContaining(
            @Param("productId") Long productId,
            @Param("query") String query,
            Pageable pageable
    );
}
