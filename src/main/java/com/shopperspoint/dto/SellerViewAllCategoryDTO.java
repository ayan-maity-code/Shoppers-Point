package com.shopperspoint.dto;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class SellerViewAllCategoryDTO {


    private Long categoryId;
    private String categoryName;
    private List<ParentCategoryDTO> parentChain;
    private List<MetadataFieldWithIdDTO> metadataFields;

    @Getter
    @Setter
    @AllArgsConstructor
    public static class ParentCategoryDTO {
        private Long parentId;
        private String parentName;
    }

}

