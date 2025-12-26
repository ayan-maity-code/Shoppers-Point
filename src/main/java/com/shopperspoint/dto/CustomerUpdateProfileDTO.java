package com.shopperspoint.dto;


import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

@AllArgsConstructor
@Getter
@Setter
public class CustomerUpdateProfileDTO {

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


    @Pattern(regexp = "^[1-9][0-9]{9}$", message = "Phone number must be 10 digits and can not starts with 0")
    private String phoneNumber;

    private MultipartFile image;

}
