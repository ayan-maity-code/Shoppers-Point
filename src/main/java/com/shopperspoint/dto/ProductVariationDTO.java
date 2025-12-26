package com.shopperspoint.dto;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ProductVariationDTO {
    private Long id;
    private Integer quantityAvailable;
    private Long price;
    private Map<String, String> metaData;
    private String primaryImageUrl;
    private Boolean isActive;
}
