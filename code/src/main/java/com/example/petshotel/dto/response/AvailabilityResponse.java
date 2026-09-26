package com.example.petshotel.dto.response;

import java.time.LocalDate;
import java.util.List;

public record AvailabilityResponse(LocalDate checkIn, LocalDate checkOut, Integer petCount, List<RoomResponse> availableRooms) {
}