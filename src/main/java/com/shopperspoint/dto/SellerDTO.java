package com.shopperspoint.dto;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SellerDTO extends UserDTO {


    @NotBlank(message = "GST cannot be empty")
    @Pattern(regexp = "^[0-9]{2}[A-Z]{5}[0-9]{4}[A-Z]{1}[1-9A-Z]{1}[Z]{1}[0-9A-Z]{1}$",
            message = "Invalid GST format")
    private String gst;


    @NotBlank(message = "Company name cannot be empty")
    @Size(min = 3, max = 255, message = "Company name must be in between 3 to 255 characters")
    @Pattern(
            regexp = "^[A-Za-z0-9&()\\-.,'](?:[A-Za-z0-9&()\\-.,' ]*[A-Za-z0-9&()\\-.,'])?$",
            message = "Company name can only contain letters, numbers, spaces, and basic punctuation, and cannot start or end with spaces"
    )
    private String companyName;


    @NotBlank(message = "Company contact cannot be empty")
    @Pattern(regexp = "^[1-9][0-9]{9}$", message = "Company contact must be 10 digits and can not starts with 0")
    private String companyContact;

}
