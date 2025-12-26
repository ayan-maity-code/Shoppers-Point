package com.shopperspoint.dto;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class CustomerResponseDTO {

    private Long id;
    private String fullName;
    private String email;
    private Boolean isActive;

}
