package com.example.petshotel.mapper;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

import com.example.petshotel.domain.entity.Room;
import com.example.petshotel.dto.request.AvailabilitySearchRequest;
import com.example.petshotel.dto.response.AvailabilityResponse;
import com.example.petshotel.dto.response.RoomResponse;

@Component
public class AvailabilityMapper {

    private final RoomMapper roomMapper;

    public AvailabilityMapper(RoomMapper roomMapper) {
        this.roomMapper = roomMapper;
    }

    public AvailabilityResponse toResponse(AvailabilitySearchRequest request, List<Room> rooms) {
        List<RoomResponse> roomResponses = new ArrayList<>();
        for (Room room : rooms) {
            roomResponses.add(roomMapper.toResponse(room));
        }
        return new AvailabilityResponse(request.checkIn(), request.checkOut(), request.petCount(), roomResponses);
    }
}