package com.shopperspoint.repository;

import com.shopperspoint.entity.CategoryMetadataField;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CategoryMetadataFieldRepo extends JpaRepository<CategoryMetadataField, Long> {


    Optional<CategoryMetadataField> findByNameIgnoreCase(String name);

    Page<CategoryMetadataField> findByNameContainingIgnoreCase(String filter, Pageable pageable);
}
