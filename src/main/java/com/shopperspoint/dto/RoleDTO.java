package com.shopperspoint.dto;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RoleDTO {

    @NotBlank(message = "Role cannot be empty")
    @Pattern(regexp = "^[A-Za-z ]+$", message = "Role can only contain letters and spaces")
    private String authority;

}
