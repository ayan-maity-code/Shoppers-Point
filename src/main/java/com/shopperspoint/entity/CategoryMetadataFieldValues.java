package com.shopperspoint.entity;


import com.shopperspoint.key.CategoryMetadataKey;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CategoryMetadataFieldValues {
    @EmbeddedId
    private CategoryMetadataKey id;

    @Column(nullable = false)
    private String fieldValues;

    @ManyToOne
    @MapsId("categoryMetadataFieldId")
    @JoinColumn(name = "category_metadata_field_id")
    private CategoryMetadataField categoryMetadataField;

    @ManyToOne
    @MapsId("categoryId")
    @JoinColumn(name = "category_id")
    private Category category;
}
