package com.shopperspoint.dto;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Set;

@Getter
@Setter
@AllArgsConstructor
public class CategoryFilterResponseDTO {

    private Long categoryId;
    private String categoryName;

    private Set<String> brands;
    private List<MetadataFieldWithIdDTO> metadataFieldValues;
    private Double minPrice;
    private Double maxPrice;
}

