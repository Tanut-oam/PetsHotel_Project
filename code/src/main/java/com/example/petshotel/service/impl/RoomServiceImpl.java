package com.example.petshotel.service.impl;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.example.petshotel.domain.entity.Room;
import com.example.petshotel.domain.enums.RoomStatus;
import com.example.petshotel.dto.request.CreateRoomRequest;
import com.example.petshotel.dto.request.UpdateRoomRequest;
import com.example.petshotel.dto.response.RoomResponse;
import com.example.petshotel.repository.RoomRepository;
import com.example.petshotel.service.RoomService;

import org.springframework.transaction.annotation.Transactional;

@Service
public class RoomServiceImpl implements RoomService {
    
    private final RoomRepository roomRepository;


    public RoomServiceImpl(RoomRepository roomRepository){
        this.roomRepository = roomRepository;
    }

    @Transactional
    public RoomResponse createRoom(CreateRoomRequest request){
        if(roomRepository.existsByRoomNumber(request.roomNumber())){
            throw new IllegalArgumentException("Room number already exists: " + request.roomNumber());
        }

        Room room = new Room();
        room.setRoomNumber(request.roomNumber());
        room.setName(request.name());
        room.setDescription(request.description());
        room.setCapacity(request.capacity());
        room.setPricePerPetPerNight(request.pricePerPetPerNight());
        room.setStatus(RoomStatus.ACTIVE);

        Room saved = roomRepository.save(room);

        return new RoomResponse(
            saved.getId(),
            saved.getRoomNumber(),
            saved.getName(),
            saved.getDescription(),
            saved.getCapacity(),
            saved.getPricePerPetPerNight(),
            saved.getStatus()
        );

    };

    @Transactional(readOnly = true)
    public  RoomResponse getRoomById(Long id){
        Room room = roomRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Room not found: " + id));
        return new RoomResponse(room.getId(),room.getRoomNumber(),room.getName(),room.getDescription(),room.getCapacity(),room.getPricePerPetPerNight(),room.getStatus());
    };

    @Transactional(readOnly  = true)
    public List<RoomResponse> getAllRooms(){
        List<RoomResponse> AllRoom = new ArrayList<>();
        for(Room room:roomRepository.findAll()){
            
            RoomResponse response = new RoomResponse(
                room.getId(),
                room.getRoomNumber(),
                room.getName(),
                room.getDescription(),
                room.getCapacity(),
                room.getPricePerPetPerNight(),
                room.getStatus()
            );
            AllRoom.add(response);
        }
        
        return AllRoom;
    };

    @Transactional
    public void deactivateRoom(Long id){
        Room room = roomRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Room not found: " + id));
        room.setStatus(RoomStatus.INACTIVE);
        roomRepository.save(room);
    }

    @Transactional
    public RoomResponse setRoomStatus(Long id, RoomStatus status){
        Room room = roomRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Room not found: " + id));
        room.setStatus(status);
        roomRepository.save(room);


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

    @Transactional
    public RoomResponse updateRoom(Long id,UpdateRoomRequest request){
        Room room = roomRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Room not found: " + id));

        //ถ้าroomใหม่ไม่เท่ากับ roomเก่า และ roomไม่ซ้ำใคร
        if (!room.getRoomNumber().equals(request.roomNumber()) && roomRepository.existsByRoomNumber(request.roomNumber())) {
            throw new IllegalArgumentException("Room number already exists: " + request.roomNumber());
        }

        room.setRoomNumber(request.roomNumber());
        room.setName(request.name());
        room.setDescription(request.description());
        room.setCapacity(request.capacity());
        room.setPricePerPetPerNight(request.pricePerPetPerNight());

        roomRepository.save(room);

        return new RoomResponse(
            room.getId(),
            room.getRoomNumber(),
            room.getName(),
            room.getDescription(),
            room.getCapacity(),
            room.getPricePerPetPerNight(),
            room.getStatus()
        );

    };
}
