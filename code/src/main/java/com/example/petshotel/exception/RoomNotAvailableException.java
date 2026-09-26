package com.example.petshotel.exception;

import java.time.LocalDate;

public class RoomNotAvailableException extends RuntimeException {

    public RoomNotAvailableException(Long roomId, LocalDate checkIn, LocalDate checkOut) {
        super("Room " + roomId + " is not available from " + checkIn + " to " + checkOut);
    }
}