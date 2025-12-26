package com.shopperspoint.dto;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CustomerDTO extends UserDTO {


    @NotBlank(message = "Phone number cannot empty")
    @Pattern(regexp = "^[1-9][0-9]{9}$", message = "Phone number must be 10 digits and cannot start with 0")
    private String phoneNumber;
}
