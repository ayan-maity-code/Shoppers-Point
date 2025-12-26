package com.shopperspoint.dto;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public class ProductViewDTO {
    private Long id;
    private String name;
    private String brand;
    private String description;
    private Boolean isCancellable;
    private Boolean isReturnable;
    private CategoryViewResponseDTO category;
    private List<String> primaryImageUrl;
}
