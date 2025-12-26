package com.shopperspoint.key;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Embeddable
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CategoryMetadataKey implements Serializable {
    private Long categoryMetadataFieldId;
    private Long categoryId;
}
