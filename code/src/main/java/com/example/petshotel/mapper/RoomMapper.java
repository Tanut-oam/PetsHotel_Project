package com.example.petshotel.mapper;

import org.springframework.stereotype.Component;
import com.example.petshotel.domain.entity.Room;
import com.example.petshotel.dto.response.RoomResponse;

@Component
public class RoomMapper {
    public RoomResponse toResponse(Room room) {
        return new RoomResponse(
            room.getId(),
            room.getRoomNumber(),
            room.getName(),
            room.getDescription(),
            room.getCapacity(),
            room.getPricePerPetPerNight(),
            room.getStatus()
        );
    }
}
