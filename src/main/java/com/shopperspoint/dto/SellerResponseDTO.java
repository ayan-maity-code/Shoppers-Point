package com.shopperspoint.dto;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public class SellerResponseDTO {
    private Long id;
    private String fullName;
    private String email;
    private Boolean isActive;
    private String companyName;
    private List<AddressDTO> companyAddress;
    private String companyContact;
}
