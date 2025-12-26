package com.shopperspoint.dto;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@AllArgsConstructor
@Setter
public class SellerViewProfileDTO {
    private Long id;
    private String firstName;
    private String lastName;
    private Boolean isActive;
    private String companyName;
    private String companyContact;
    private String GST;
    private AddressDTO companyAddress;
    private String profileImageUrl;
}
