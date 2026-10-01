package com.example.petshotel.dto.response;

import java.time.LocalDate;

public record ReportableBookingPetResponse(
    Long bookingPetId,
    Long bookingId,
    String petName,
    String ownerName,
    LocalDate checkInDate,
    LocalDate checkOutDate
) {
    
}
