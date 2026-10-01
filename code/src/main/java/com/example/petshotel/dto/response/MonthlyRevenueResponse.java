package com.example.petshotel.dto.response;

import java.math.BigDecimal;

public record MonthlyRevenueResponse(
        int month,
        String monthName,
        BigDecimal totalAmount
) {}