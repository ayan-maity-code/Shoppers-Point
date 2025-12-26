package com.shopperspoint.repository;

import com.shopperspoint.entity.CategoryMetadataFieldValues;
import com.shopperspoint.key.CategoryMetadataKey;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CategoryMetadataFieldValuesRepo extends JpaRepository<CategoryMetadataFieldValues, CategoryMetadataKey> {

    Optional<CategoryMetadataFieldValues> findByCategoryIdAndCategoryMetadataFieldId(Long categoryId, Long fieldId);

    boolean existsByCategoryIdAndCategoryMetadataFieldId(Long categoryId, Long fieldId);

    List<CategoryMetadataFieldValues> findByCategoryId(Long id);

    List<CategoryMetadataFieldValues> findByCategoryMetadataFieldId(Long id);


    List<CategoryMetadataFieldValues> findFieldValuesByCategoryIdAndCategoryMetadataFieldId(Long categoryId, Long fieldId);

}
