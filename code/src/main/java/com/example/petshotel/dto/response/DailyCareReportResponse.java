package com.example.petshotel.dto.response;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record DailyCareReportResponse(
    Long id,
    Long bookingPetId,
    Long bookingId,
    Long petId,
    String petName,
    Long recordedById,
    LocalDate reportDate,
    String feedingMorning,
    String feedingEvening,
    Integer walkingMinutes,
    String grooming,
    String mood,
    String healthNote,
    String generalNote,
    LocalDateTime createdAt
) {}