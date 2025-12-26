package com.shopperspoint.dto;


import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ProductVariationUpdateDTO {
    @Min(value = 0, message = "Quantity available must be 0 or more")
    private Integer quantityAvailable;

    @Min(value = 1, message = "Price must be at least 1")
    private Long price;

    @Size(min = 1, message = "At least one metadata field must be provided")
    private Map<String, String> metaData;

    private MultipartFile primaryImage;

    List<MultipartFile> secondaryImages;

    private Boolean isActive;
}
