package com.example.petshotel.dto.response;

import java.util.List;

import com.example.petshotel.domain.enums.BookingStatus;

public record RecentBookingResponse(
    Long id,
    String customerName,
    List<String> petNames,
    String roomNumber,
    BookingStatus status
) {
}