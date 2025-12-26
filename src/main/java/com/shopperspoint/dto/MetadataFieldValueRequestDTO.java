package com.shopperspoint.dto;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MetadataFieldValueRequestDTO {

    @NotBlank(message = "Values must not be blank")
    @Pattern(
            regexp = "^\\s*\\w+(\\s*,\\s*\\w+)*\\s*$",
            message = "Values must be comma-separated and valid"
    )
    private String values;

    @NotNull(message = "Metadata field ID is required")
    private Long categoryMetadataFieldId;
}
