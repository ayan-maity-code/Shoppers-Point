package com.shopperspoint.dto;


import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PasswordDTO {

    @NotBlank(message = "Password cannot be empty")
    @Pattern(
            regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&]).{8,15}$",
            message = "Password must contain 1 uppercase, 1 lowercase, 1 special character, and 1 number & Password must be 8-15 characters long"
    )
    private String password;


    @NotBlank(message = "Confirm password cannot be empty")
    @Pattern(
            regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&]).{8,15}$",
            message = "Confirm password must contain 1 uppercase, 1 lowercase, 1 special character, and 1 number & Password must be 8-15 characters long"
    )
    private String confirmPassword;

    @AssertTrue(message = "Password and confirm password doesn't match. Please verify your password")
    public boolean isPasswordMatched() {
        return password != null && password.equals(confirmPassword);
    }

}
