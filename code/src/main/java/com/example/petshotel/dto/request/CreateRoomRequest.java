package com.example.petshotel.dto.request;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateRoomRequest(
    @NotBlank(message = "RoomNumber is required")
    String roomNumber,

    @NotBlank(message = "Name is required")
    String name,

    String description,

    @NotNull(message = "Please specify the number of animals accommodated.")
    @Min(value = 1, message = "The room must accommodate at least one animal.")

    Integer capacity,

    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.00", message = "Price must not be negative")
    @Digits(integer = 8, fraction = 2, message = "Price must have at most 8 digits and 2 decimal places")
    BigDecimal pricePerPetPerNight
) {}