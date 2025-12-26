package com.shopperspoint.dto;


import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@Getter
@Setter
public class ProductVariationRequestDTO {

    @NotNull(message = "Product ID is required")
    private Long productId;

    @NotNull(message = "Metadata is required")
    private Map<String, String> metaData;

    @NotNull(message = "Primary image is required")
    private MultipartFile primaryImage;

    @Min(value = 0, message = "Quantity must be 0 or more")
    private Integer quantityAvailable;

    @Min(value = 0, message = "Price must be 0 or more")
    private Long price;

    List<MultipartFile> secondaryImages;
}
