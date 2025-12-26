package com.shopperspoint.dto;


import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class CustomerViewProfileDTO {

    private Long id;
    private String firstName;
    private String lastName;
    private Boolean isActive;
    private String contact;
    private String profileImageUrl;

}
