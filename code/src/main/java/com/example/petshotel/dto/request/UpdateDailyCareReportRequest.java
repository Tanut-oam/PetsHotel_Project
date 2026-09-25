package com.example.petshotel.dto.request;

import java.time.LocalDate;

import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.*;

public record UpdateDailyCareReportRequest(
    @NotNull(message = "Booking pet ID is required")
    @Positive(message = "Booking pet ID must be greater than zero")
    Long bookingPetId,
    
    @NotNull(message = "Report date is required")
    LocalDate reportDate,
    
    String feedingMorning,
    String feedingEvening,

    @PositiveOrZero(message = "Walking minutes must be zero or greater")
    Integer walkingMinutes,

    String grooming,
    String mood,
    String healthNote,
    String generalNote
) {
    
}
