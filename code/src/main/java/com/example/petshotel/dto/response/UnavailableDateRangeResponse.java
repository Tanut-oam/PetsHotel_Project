package com.example.petshotel.dto.response;

import java.time.LocalDate;

public record UnavailableDateRangeResponse(
        LocalDate unavailableFrom,
        LocalDate availableAgainOn
) {
}