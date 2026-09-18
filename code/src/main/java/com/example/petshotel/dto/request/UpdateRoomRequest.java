package com.example.petshotel.dto.request;

import java.math.BigDecimal;

public record UpdateRoomRequest(String roomNumber,String name,String description,Integer capacity,BigDecimal pricePerPetPerNight) {
}