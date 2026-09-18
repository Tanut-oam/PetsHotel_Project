package com.example.petshotel.dto.response;

import java.math.BigDecimal;

import com.example.petshotel.domain.enums.RoomStatus;

public record RoomResponse(Long id,String roomNumber,String name,String description,Integer capacity,BigDecimal pricePerPetPerNight,RoomStatus status) {
}
