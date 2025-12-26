package com.shopperspoint.dto;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public class MetadataFieldWithIdDTO {
    private Long metadataFieldId;

    @Size(min = 3, max = 255, message = "Metadata field name must be between 5 and 255 characters")
    @Pattern(regexp = "^(?!\\s)(?!.*\\s$)[A-Za-z0-9 ]+$", message = "Metadata field name can only contain letters, numbers, " +
            "and spaces, without leading or trailing spaces")
    private String metadataFieldName;

    private List<String> values;
}
