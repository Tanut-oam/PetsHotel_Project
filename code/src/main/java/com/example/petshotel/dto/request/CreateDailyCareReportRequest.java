package com.example.petshotel.dto.request;

import java.time.LocalDate;

import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.NotNull;

public record CreateDailyCareReportRequest(
    @NotNull(message = "Report date is required")
    LocalDate reportDate,

    String feedingMoString,
    String feedingEvening,
    
    @PositiveOrZero(message = "Walking minutes must be zero or greater")
    Integer walkingMinutes,

    String grooming,
    String mood,
    String healthNote,
    String generalNote
) {}
