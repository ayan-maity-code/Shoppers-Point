package com.shopperspoint.dto;


import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class ParentCategoryDTO {
    private Long id;

    @Size(min = 2, max = 255, message = "Parent category name must be between 2 and 255 characters")
    @Pattern(regexp = "^(?!\\s)(?!.*\\s$)[A-Za-z0-9 ]+$", message = "Parent category name can only contain letters, numbers, " +
            "and spaces, without leading or trailing spaces")
    private String name;
    private ParentCategoryDTO parentCategory;
}
