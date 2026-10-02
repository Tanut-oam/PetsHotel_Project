package com.example.petshotel.dto.response;

import java.time.LocalDate;
import java.util.List;

public record RoomAvailabilityCalendarResponse(
        Long roomId,
        LocalDate fromDate,
        LocalDate toDate,
        List<UnavailableDateRangeResponse> unavailableRanges
) {
}