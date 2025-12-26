package com.shopperspoint.dto;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProductDTO {
    @NotBlank(message = "Product name is required")
    @Size(min = 2, max = 255, message = "Product name must be between 2 and 255 characters")
    @Pattern(regexp = "^(?!\\s)(?=.*[A-Za-z])[A-Za-z0-9'|,\\-./&():_ ]*(?<!\\s)$",
            message = "Product name must contain at least one letter, and cannot start or end with spaces")
    private String name;

    @NotBlank(message = "Brand is required")
    @Size(min = 2, max = 255, message = "Brand name must be between 2 and 255 characters")
    @Pattern(regexp = "^(?!\\s)(?=.*[A-Za-z])[A-Za-z0-9'&\\-./_ ]*(?<!\\s)$",
            message = "Brand name must contain at least one letter, and cannot start or end with spaces")
    private String brand;

    @NotNull(message = "Category ID is required")
    private Long categoryId;


    @Size(min = 5, max = 255, message = "Description name must be between 5 and 255 characters")
    @Pattern(regexp = "^(?!\\s)(?=.*[A-Za-z])[A-Za-z0-9'|,\\-./()_ ]*(?<!\\s)$",
            message = "Description must contain at least one letter, and cannot start or end with spaces")
    private String description;

    private Boolean isCancellable = false;
    private Boolean isReturnable = false;

}
