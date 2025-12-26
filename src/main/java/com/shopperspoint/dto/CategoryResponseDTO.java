package com.shopperspoint.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public class CategoryResponseDTO {

    private Long id;
    private String name;
    private ParentCategoryDTO parentCategory;
    private List<SimpleCategoryDTO> childCategoryList;
    private List<MetadataFieldDTO> metadataFieldList;
}
