package com.example.petshotel.dto.response;

import java.time.LocalDate;
import java.util.List;

public record PetAvailabilityResponse(
        LocalDate checkInDate,
        LocalDate checkOutDate,
        List<Long> unavailablePetIds
) {
}