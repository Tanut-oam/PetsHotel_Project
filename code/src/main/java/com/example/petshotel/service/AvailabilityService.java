package com.example.petshotel.service;

import java.time.LocalDate;

public interface AvailabilityService {
    boolean isRoomAvailable(Long roomId, LocalDate checkIn, LocalDate checkOut, int petCount);
}