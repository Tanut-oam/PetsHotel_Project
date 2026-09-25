package com.example.petshotel.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.example.petshotel.domain.enums.PromotionType;

public record PromotionResponse(
    Long id,
    String name,
    String description,
    PromotionType type,
    BigDecimal value,
    LocalDate startDate,
    LocalDate endDate,
    Integer minimumNights,
    Boolean active
) {
} 