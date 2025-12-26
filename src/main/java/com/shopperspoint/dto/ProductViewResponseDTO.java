package com.shopperspoint.dto;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ProductViewResponseDTO {

    private Long id;
    private String name;
    private String brand;
    private String description;
    private Boolean isCancellable;
    private Boolean isReturnable;
    private CategoryViewResponseDTO category;
    private List<ProductVariationDTO> variations;
}
