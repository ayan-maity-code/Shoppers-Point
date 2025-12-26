package com.shopperspoint.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Set;


@Getter
@Setter
public class UserDTO {
    @NotBlank(message = "First name cannot be empty")
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

    @NotBlank(message = "Email cannot be empty")
    @Pattern(
            regexp = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,6}$",
            message = "Invalid email format, valid format is(name@example.com)"
    )
    @Size(min = 3, max = 255, message = "Email must be in between 3 to 255 characters")
    private String email;

    @NotBlank(message = "Password cannot be empty")
    @Pattern(
            regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&]).{8,15}$",
            message = "Password must contain 1 uppercase, 1 lowercase, 1 special character, and 1 number & Password must be 8-15 characters long"
    )
    @Size(min = 3, max = 255, message = "Password must be in between 3 to 255 characters")
    private String password;

    @NotBlank(message = "Confirm Password cannot be empty")
    private String confirmPassword;


    private Integer invalidAttemptCount = 0;

    @Valid
    private Set<RoleDTO> roles;

    @Valid
    private List<AddressDTO> address;

    @AssertTrue(message = "Password and confirm password doesn't match. Please verify your password")
    public boolean isPasswordNotMatched() {
        return password != null && password.equals(confirmPassword);
    }
}
