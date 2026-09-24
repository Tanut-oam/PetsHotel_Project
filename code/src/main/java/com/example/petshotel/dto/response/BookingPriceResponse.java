package com.example.petshotel.dto.response;

import java.math.BigDecimal;

public record BookingPriceResponse(
        BigDecimal basePrice,
        BigDecimal extraServicesPrice,
        BigDecimal holidaySurcharge,
        BigDecimal discountAmount,
        BigDecimal totalPrice
) {}