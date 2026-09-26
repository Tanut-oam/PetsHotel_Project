package com.example.petshotel.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.petshotel.domain.entity.Room;
import com.example.petshotel.domain.enums.RoomStatus;
import com.example.petshotel.dto.request.CreateRoomRequest;
import com.example.petshotel.dto.request.UpdateRoomRequest;
import com.example.petshotel.dto.request.UpdateStatusRequest;
import com.example.petshotel.dto.response.RoomResponse;
import com.example.petshotel.mapper.RoomMapper;
import com.example.petshotel.repository.RoomRepository;
import com.example.petshotel.service.impl.RoomServiceImpl;

@ExtendWith(MockitoExtension.class)
public class RoomServiceTest {
    
    @Mock
    private RoomRepository roomRepository;

    @Mock
    private RoomMapper roomMapper;

    @InjectMocks
    private RoomServiceImpl roomService;

    private Room room;
    private RoomResponse roomResponse;
    
    @BeforeEach
    void setUp(){
        room = new Room();
        room.setId(1L);
        room.setRoomNumber("101");
        room.setName("Deluxe");
        room.setDescription("ห้องมาตรฐาน");
        room.setCapacity(3);
        room.setPricePerPetPerNight(new BigDecimal("500"));
        room.setStatus(RoomStatus.ACTIVE);

        roomResponse = new RoomResponse(room.getId(),room.getRoomNumber(),room.getName(),room.getDescription(),room.getCapacity(),room.getPricePerPetPerNight(),room.getStatus());
    }

    //ควรโยน_exception_เมื่อเลขห้องซ้ำ
    @Test
    void createRoom(){
        CreateRoomRequest request = new CreateRoomRequest("101", "Deluxe", "ห้องมาตรฐาน",
                3, new BigDecimal("500"));
        when(roomRepository.existsByRoomNumber("101")).thenReturn(false);
        when(roomRepository.save(any(Room.class))).thenReturn(room);
        when(roomMapper.toResponse(room)).thenReturn(roomResponse);
        
        RoomResponse result = roomService.createRoom(request);
        assertNotNull(result);
        assertEquals("101", result.roomNumber());
        verify(roomRepository).save(any(Room.class));
    }

    
    @Test
    void getRoomById(){
        when(roomRepository.findById(1L)).thenReturn(Optional.of(room));
        when(roomMapper.toResponse(room)).thenReturn(roomResponse);

        RoomResponse result = roomService.getRoomById(1L);
        assertEquals("101", result.roomNumber());
    }

    //เมื่อไม่เจอห้อง ID
    @Test
    void getRoomById_exception(){
        when(roomRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(IllegalArgumentException.class, () -> roomService.getRoomById(99L));
    }

    @Test
    void getAllRooms(){
        when(roomRepository.findAll()).thenReturn(List.of(room));
        when(roomMapper.toResponse(room)).thenReturn(roomResponse);

        List<RoomResponse> result = roomService.getAllRooms();
        assertEquals(1, result.size());
        assertEquals("101", result.get(0).roomNumber());
    }

    @Test
    void updateRoom() {
        UpdateRoomRequest request = new UpdateRoomRequest("102", "Deluxe V2", "อัปเดตแล้ว",
                4, new BigDecimal("600"));
        when(roomRepository.findById(1L)).thenReturn(Optional.of(room));
        when(roomRepository.existsByRoomNumber("102")).thenReturn(false);
        when(roomMapper.toResponse(room)).thenReturn(roomResponse);

        roomService.updateRoom(1L, request);

        verify(roomRepository).save(room);
    }

    @Test
    void updateRoom_exception() {
        UpdateRoomRequest request = new UpdateRoomRequest("999", "Deluxe V2", "อัปเดตแล้ว",
                4, new BigDecimal("600"));
        when(roomRepository.findById(1L)).thenReturn(Optional.of(room));
        when(roomRepository.existsByRoomNumber("999")).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () -> roomService.updateRoom(1L, request));
        verify(roomRepository, never()).save(any(Room.class));
    }

    @Test
    void setRoomStatus() {
        UpdateStatusRequest request = new UpdateStatusRequest(RoomStatus.MAINTENANCE);
        when(roomRepository.findById(1L)).thenReturn(Optional.of(room));
        when(roomMapper.toResponse(room)).thenReturn(roomResponse);

        roomService.setRoomStatus(1L, request);

        assertEquals(RoomStatus.MAINTENANCE, room.getStatus());
        verify(roomRepository).save(room);
    }

    @Test
    void deactivateRoom() {
        when(roomRepository.findById(1L)).thenReturn(Optional.of(room));
        when(roomMapper.toResponse(room)).thenReturn(roomResponse);

        roomService.deactivateRoom(1L);

        assertEquals(RoomStatus.INACTIVE, room.getStatus());
        verify(roomRepository).save(room);
    }
}
