package com.example.petshotel.dto.request;
import java.math.BigDecimal;
import java.time.LocalDate;

import com.example.petshotel.domain.enums.PromotionType;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record CreatePromotionRequest(
    @NotBlank(message = "Promotion name is required")
    String name,

    @NotNull(message = "Promotion type is required")
    PromotionType type,

    @NotNull(message = "Discount value is required")
    @Positive(message = "Discount value must be greater than zero")
    BigDecimal discountValue,

    @NotNull(message = "Start date is required")
    LocalDate startDate,

    @NotNull(message = "End date is required")
    LocalDate endDate,

    boolean active
) {}
