package com.shopperspoint.dto;


import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class SellerUpdateProfileDTO {

    @Pattern(
            regexp = "^[a-zA-Z]+$",
            message = "First name must contain only letters"
    )
    @Size(min = 3, max = 255, message = "First name must be in between 3 to 255 characters")
    private String firstName;

    @Pattern(
            regexp = "^[a-zA-Z]+$",
            message = "Middle name must contain only letters"
    )
    @Size(min = 3, max = 255, message = "Middle name must be in between 3 to 255 characters")
    private String middleName;

    @Pattern(
            regexp = "^[a-zA-Z]+$",
            message = "Last name must contain only letters"
    )
    @Size(min = 3, max = 255, message = "Last name must be in between 3 to 255 characters")
    private String lastName;


    @Pattern(regexp = "^[0-9]{2}[A-Z]{5}[0-9]{4}[A-Z]{1}[1-9A-Z]{1}[Z]{1}[0-9A-Z]{1}$",
            message = "Invalid GST format")
    private String gst;


    @Pattern(regexp = "^[1-9][0-9]{9}$", message = "Company contact must be 10 digits and can not starts with 0")
    private String companyContact;

    @Pattern(
            regexp = "^[A-Za-z0-9&()\\-.,'](?:[A-Za-z0-9&()\\-.,' ]*[A-Za-z0-9&()\\-.,'])?$",
            message = "Company name can only contain letters, numbers, spaces, and basic punctuation, and cannot start or end with spaces"
    )
    @Size(min = 3, max = 255, message = "Company name must be in between 3 to 255 characters")
    private String companyName;

    private MultipartFile image;

}
