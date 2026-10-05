package com.example.petshotel.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public record RevenueBookingResponse(
        Long bookingId,
        String customerName,
        List<String> petNames,
        String roomNumber,
        LocalDate checkInDate,
        LocalDate checkOutDate,
        LocalDateTime paidAt,
        String receiptNumber,
        BigDecimal roomAmount,
        BigDecimal serviceAmount,
        BigDecimal surchargeAmount,
        BigDecimal discountAmount,
        BigDecimal totalAmount
) {}