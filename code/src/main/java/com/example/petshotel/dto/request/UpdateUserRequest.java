package com.example.petshotel.dto.request;

import com.example.petshotel.domain.enums.UserRole;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public record UpdateUserRequest(
        @NotBlank(message = "Please enter your name.")
        String firstName,

        @NotBlank(message = "Please enter your surname.")
        String lastName,

        @NotBlank(message = "Please enter your email address.")
        @Email(message = "Invalid email format.")
        String email,

        @NotBlank(message = "Please enter your phone number.")
        @Pattern(regexp = "^0\\d{8,9}$", message = "The phone number must start with 0 and consist of 9–10 digits.")
        String phoneNumber,

        @NotNull(message = "Please select a role.")
        UserRole role,

        @NotNull(message = "Please select the account status.")
        Boolean active
) {
}