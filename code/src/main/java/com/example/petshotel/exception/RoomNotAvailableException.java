package com.example.petshotel.exception;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class RoomNotAvailableException extends RuntimeException {

    private static final DateTimeFormatter DATE_FORMAT =
            DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public RoomNotAvailableException(
            Long roomId,
            LocalDate checkIn,
            LocalDate checkOut) {

        super("ห้องที่เลือกไม่พร้อมให้จองในช่วงวันที่ "
                + checkIn.format(DATE_FORMAT)
                + " ถึง "
                + checkOut.format(DATE_FORMAT)
                + " กรุณาเลือกห้องหรือช่วงวันอื่น");
    }

    private RoomNotAvailableException(String message) {
        super(message);
    }

    public static RoomNotAvailableException alreadyBooked(
            LocalDate checkIn,
            LocalDate checkOut) {

        return new RoomNotAvailableException(
                "ห้องนี้มีการจองในช่วงวันที่ "
                        + checkIn.format(DATE_FORMAT)
                        + " ถึง "
                        + checkOut.format(DATE_FORMAT)
                        + " แล้ว จึงไม่สามารถยืนยันการจองของคุณได้"
                        + " กรุณาเลือกห้องอื่นหรือเปลี่ยนช่วงวันเข้าพัก"
        );
    }
}