package com.example.petshotel.exception;

import java.time.LocalDate;
import java.util.List;

public class PetNotAvailableException extends IllegalStateException {

    public PetNotAvailableException(
            List<Long> petIds,
            LocalDate checkIn,
            LocalDate checkOut
    ) {
        super(
                "Pets already have overlapping bookings: "
                        + petIds
                        + " between "
                        + checkIn
                        + " and "
                        + checkOut
        );
    }
}