package com.shopperspoint.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ProductResponseDTO {
    private Long id;
    private String name;
    private String brand;
    private String description;
    private Boolean isCancellable;
    private Boolean isReturnable;
    private Boolean isActive;

    private Long categoryId;
    private String categoryName;
}
