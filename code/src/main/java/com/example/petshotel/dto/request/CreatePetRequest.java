package com.example.petshotel.dto.request;
import com.example.petshotel.domain.enums.PetType;

import jakarta.validation.constraints.*;

public record CreatePetRequest (
    @NotBlank(message = "Pet name is required")
    String name,
    @NotNull(message = "Pet type is required")
    PetType type,
    @PositiveOrZero(message = "Age must be zero or greater")
    Integer age,
    @Positive(message = "Weight must be greater than zero")
    Double weight,

    String breed,
    String gender,
    String medicalNote,
    String feedingInstruction,
    String specialNote
){}