package com.example.petshotel.service;

import java.util.List;

import com.example.petshotel.domain.enums.RoomStatus;
import com.example.petshotel.dto.request.CreateRoomRequest;
import com.example.petshotel.dto.request.UpdateRoomRequest;
import com.example.petshotel.dto.response.RoomResponse;

public interface RoomService {
    RoomResponse createRoom(CreateRoomRequest request);
    RoomResponse getRoomById(Long id);
    List<RoomResponse> getAllRooms();
    RoomResponse updateRoom(Long id,UpdateRoomRequest request);
    void deactivateRoom(Long id);
    RoomResponse setRoomStatus(Long id,RoomStatus status);
}
