package com.example.petshotel.dto.response;

import com.example.petshotel.domain.enums.PetType;

public record PetResponse(
    Long id,
    String name,
    PetType type,
    String breed,
    Integer age,
    Double weight,
    String gender,
    String medicalNote,
    String feedingInstruction,
    String specialNote,
    Long ownerId,
    Boolean active
) {}