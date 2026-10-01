package com.example.petshotel.dto.request;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

public record CreateBookingApiRequest(

        @NotNull(message = "Room ID is required")
        Long roomId,

        @NotEmpty(message = "At least one pet is required")
        List<@NotNull(message = "Pet ID must not be null") Long> petIds,

        @NotNull(message = "Check-in date is required")
        LocalDate checkInDate,

        @NotNull(message = "Check-out date is required")
        LocalDate checkOutDate,

        Map<Long, List<Long>> servicePetIds,

        Long promotionId
) {
}
