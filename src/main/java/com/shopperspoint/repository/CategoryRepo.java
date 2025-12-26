package com.shopperspoint.repository;

import com.shopperspoint.entity.Category;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CategoryRepo extends JpaRepository<Category, Long> {
    Optional<Category> findByNameIgnoreCaseAndParentCategoryId(String name, Long parentCategory);

    Optional<Category> findByNameIgnoreCaseAndParentCategoryIsNull(String name);

    Page<Category> findByNameContainingIgnoreCase(String filter, Pageable pageable);

    List<Category> findByParentCategoryId(Long parentId);

    List<Category> findByParentCategory(Category parent);

    List<Category> findByParentCategoryIsNull();

    boolean existsByParentCategoryId(Long id);

    boolean existsByName(String name);

}
