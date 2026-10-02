package com.example.petshotel.exception;

import java.time.LocalDate;

public class RoomNotAvailableException extends RuntimeException {

    public RoomNotAvailableException(Long roomId, LocalDate checkIn, LocalDate checkOut) {
        super("ห้องที่เลือกไม่ว่างในช่วงวันที่ "
        + checkIn + " ถึง " + checkOut
        + " กรุณาเลือกห้องหรือช่วงวันอื่น");
    }
}