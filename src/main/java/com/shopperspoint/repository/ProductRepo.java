package com.shopperspoint.repository;

import com.shopperspoint.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ProductRepo extends JpaRepository<Product, Long> {

    List<Product> findByCategoryIdIn(List<Long> categoryIds);

    boolean existsByNameIgnoreCaseAndBrandAndCategoryIdAndSellerId(String name, String brand, Long categoryId, Long sellerId);

    Page<Product> findBySellerIdAndIsDeletedFalse(Long sellerId, Pageable pageable);


    @Query("""
            select p from Product p
            where p.seller.id = :sellerId
            and(
                :query is null or
                lower(p.name) like lower(concat('%', :query, '%')) or
                lower(p.brand) like lower(concat('%', :query, '%'))
            )
            """)
    Page<Product> findProductsBySeller(
            @Param("sellerId") Long sellerId,
            @Param("query") String query,
            Pageable pageable
    );

    @Query(
            """
                    select p from Product p
                    where p.isDeleted = false and
                    p.isActive = true and
                    p.category.id in :categoryIds
                    and(
                        :query is null or
                        lower(p.name) like lower(concat('%', :query, '%')) or
                        lower(p.brand) like lower(concat('%', :query, '%')) or
                        lower(p.description) like lower(concat('%', :query, '%'))
                    )
                    """
    )
    Page<Product> findByAllCategoryIdsAndFilter(
            @Param("categoryIds") List<Long> categoryIds,
            @Param("query") String query,
            Pageable pageable
    );

    Page<Product> findByCategoryIdInAndIsActiveTrueAndIsDeletedFalse(List<Long> categoryIds, Pageable pageable);

    Page<Product> findByCategoryIdAndIsActiveTrueAndIsDeletedFalse(Long categoryId, Pageable pageable);

    Page<Product> findByIsDeletedFalseAndIsActiveTrue(Pageable pageable);

    @Query("""
                SELECT p FROM Product p
                WHERE p.isActive = true AND
                      p.isDeleted = false AND (
                    :query IS NULL OR
                    lower(p.name) LIKE lower(concat('%', :query, '%')) OR
                    lower(p.brand) LIKE lower(concat('%', :query, '%')) OR
                    lower(p.description) LIKE lower(concat('%', :query, '%')) OR
                    str(p.category.id) LIKE concat('%', :query, '%') OR
                    str(p.seller.id) LIKE concat('%', :query, '%')
                )
            """)
    Page<Product> findActiveProductWithQuery(
            @Param("query") String query,
            Pageable pageable
    );

    Optional<Product> findByIdAndIsActiveTrueAndIsDeletedFalse(Long productId);


    @Query(
            """
                    select p from Product p
                    where p.isDeleted = false and
                    p.isActive = true and
                    p.category.id = :categoryId
                    and(
                        :query is null or
                        lower(p.name) like lower(concat('%', :query, '%')) or
                        lower(p.brand) like lower(concat('%', :query, '%')) or
                        lower(p.description) like lower(concat('%', :query, '%'))
                    )
                    """
    )
    Page<Product> findActiveProductsByCategoryIdAndQuery(
            @Param("categoryId") Long categoryId,
            @Param("query") String filter,
            Pageable pageable
    );
}
