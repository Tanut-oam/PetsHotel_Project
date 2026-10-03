package com.example.petshotel.dto.response;

import java.time.LocalDate;

import com.example.petshotel.domain.enums.PetType;

public record ReportableBookingPetResponse(
        Long bookingPetId,
        Long bookingId,
        String petName,
        String ownerName,
        String ownerEmail,
        LocalDate checkInDate,
        LocalDate checkOutDate,
        PetType type,
        String breed,
        Integer age,
        Double weight,
        String gender,
        String medicalNote,
        String feedingInstruction,
        String specialNote
) {
    
}
