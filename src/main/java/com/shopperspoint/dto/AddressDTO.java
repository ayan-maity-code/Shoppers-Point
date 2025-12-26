package com.shopperspoint.dto;


import com.shopperspoint.validation.OnUpdate;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import jakarta.validation.groups.Default;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class AddressDTO {

    @NotBlank(message = "Address Line cannot be empty")
    @Pattern(
            regexp = "^(?!\\s)([A-Za-z0-9,. ]*\\S)?$",
            message = "Address line must contain only letters, numbers, and spaces, and cannot start or end with spaces",
            groups = {Default.class, OnUpdate.class}
    )
    @Size(min = 5, max = 255, message = "Address Line must be between 5 and 255 characters"
            , groups = {Default.class, OnUpdate.class})
    private String addressLine;

    @NotBlank(message = "Label cannot be empty")
    @Pattern(regexp = "^(?!\\s)([A-Za-z ]*\\S)?$", message = "Label must contain only letters and spaces, " +
            "and cannot start or end with spaces", groups = {Default.class, OnUpdate.class})
    @Size(min = 3, max = 255, message = "Label must be between 3 and 255 characters",
            groups = {Default.class, OnUpdate.class})
    private String label;

    @NotBlank(message = "City cannot be empty")
    @Pattern(regexp = "^(?!\\s)([A-Za-z ]*\\S)?$", message = "City must contain only letters and spaces, " +
            "and cannot start or end with spaces", groups = {Default.class, OnUpdate.class})
    @Size(min = 3, max = 255, message = "City must be between 3 and 255 characters",
            groups = {Default.class, OnUpdate.class})
    private String city;

    @NotBlank(message = "State cannot be empty")
    @Pattern(regexp = "^(?!\\s)([A-Za-z ]*\\S)?$", message = "State must contain only letters and spaces, " +
            "and cannot start or end with spaces", groups = {Default.class, OnUpdate.class})
    @Size(min = 3, max = 255, message = "State must be between 3 and 255 characters",
            groups = {Default.class, OnUpdate.class})
    private String state;

    @NotBlank(message = "Country cannot be empty")
    @Pattern(regexp = "^(?!\\s)([A-Za-z ]*\\S)?$", message = "Country must contain only letters and spaces," +
            " and cannot start or end with spaces", groups = {Default.class, OnUpdate.class})
    @Size(min = 3, max = 255, message = "Country must be between 3 and 255 characters",
            groups = {Default.class, OnUpdate.class})
    private String country;

    @NotBlank(message = "Zip code cannot be empty")
    @Pattern(
            regexp = "^[1-9][0-9]{5}$",
            message = "Invalid zip code",
            groups = {Default.class, OnUpdate.class}
    )
    private String zipCode;


}
