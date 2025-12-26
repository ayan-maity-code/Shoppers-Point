package com.shopperspoint.dto;


import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public class MetadataFieldDTO {

    @Size(min = 1, max = 255, message = "Metadata field name must be between 1 and 255 characters")
    private String fieldName;

    private List<String> values;
}
