package com.shopperspoint.dto;


import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class CategoryUpdateDTO {

    private Long id;

    @Pattern(
            regexp = "^(?!\\s)(?!.*\\s$)[A-Za-z&,' ]+$",
            message = "Category name can only contain letters, spaces, and ' & , ' ', and cannot start or end with a space"
    )
    @Size(min = 3, max = 255, message = "Category name must be between 3 and 255 characters")
    private String name;
}
