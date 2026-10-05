package com.example.petshotel.dto.response;

import java.math.BigDecimal;
import java.util.List;

public record MonthlyRevenueResponse(
        int month,
        String monthName,
        BigDecimal totalAmount,
        List<RevenueBookingResponse> bookings
) {}