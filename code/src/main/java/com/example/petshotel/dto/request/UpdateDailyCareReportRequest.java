package com.example.petshotel.dto.request;

import jakarta.validation.constraints.PositiveOrZero;

public record UpdateDailyCareReportRequest(
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
