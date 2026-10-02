package com.example.petshotel.service;

import java.time.LocalDate;
import java.util.List;

import com.example.petshotel.domain.entity.Room;
import com.example.petshotel.dto.response.RoomAvailabilityCalendarResponse;

public interface AvailabilityService {

    boolean isRoomAvailable(
            Long roomId,
            LocalDate checkIn,
            LocalDate checkOut,
            int petCount
    );

    List<Room> findAvailableRooms(
            LocalDate checkIn,
            LocalDate checkOut,
            int petCount
    );

    void checkRoomAvailable(
            Long roomId,
            LocalDate checkIn,
            LocalDate checkOut,
            int petCount
    );

    RoomAvailabilityCalendarResponse getRoomAvailabilityCalendar(
            Long roomId,
            LocalDate fromDate,
            LocalDate toDate
    );
}