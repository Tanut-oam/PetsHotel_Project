package com.example.petshotel.dto.response;

import java.time.LocalDate;

public record ReportBookingSummary(
        Long id,
        LocalDate checkInDate,
        LocalDate checkOutDate
) {
} 