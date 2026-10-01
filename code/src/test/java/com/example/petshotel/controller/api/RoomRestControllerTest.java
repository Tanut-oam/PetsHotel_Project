package com.example.petshotel.controller.api;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.example.petshotel.domain.enums.RoomStatus;
import com.example.petshotel.dto.request.CreateRoomRequest;
import com.example.petshotel.dto.request.UpdateRoomRequest;
import com.example.petshotel.dto.response.RoomResponse;
import com.example.petshotel.exception.GlobalExceptionHandler;
import com.example.petshotel.service.RoomService;

import com.example.petshotel.exception.DuplicateResourceException;
import com.example.petshotel.exception.ResourceNotFoundException;

class RoomRestControllerTest {

    private RoomService roomService;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        roomService = mock(RoomService.class);

        RoomRestController controller = new RoomRestController(roomService);

        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();

        mockMvc = MockMvcBuilders.standaloneSetup(controller)
        .setControllerAdvice(new GlobalExceptionHandler()).build();
    }

    @Test
    void getAllRoomsShouldReturnRoomList() throws Exception {
        when(roomService.getAllRooms()).thenReturn(List.of(createRoomResponse()));

        mockMvc.perform(get("/api/room"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].roomNumber").value("101"));

        verify(roomService).getAllRooms();
    }

    @Test
    void getRoomByIdShouldReturnRoom() throws Exception {
        when(roomService.getRoomById(1L)).thenReturn(createRoomResponse());

        mockMvc.perform(get("/api/room/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.roomNumber").value("101"))
                .andExpect(jsonPath("$.capacity").value(3));

        verify(roomService).getRoomById(1L);
    }

    @Test
    void createRoomShouldReturnCreatedRoom() throws Exception {
        when(roomService.createRoom(any(CreateRoomRequest.class)))
                .thenReturn(createRoomResponse());

        mockMvc.perform(post("/api/room")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                    "roomNumber": "101",
                    "name": "Deluxe",
                    "description": "ห้องมาตรฐาน",
                    "capacity": 3,
                    "pricePerPetPerNight": 500
                    }
                    """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.roomNumber").value("101"));

        verify(roomService).createRoom(any(CreateRoomRequest.class));
    }

    @Test
    void updateRoomShouldReturnUpdatedRoom() throws Exception {
        when(roomService.updateRoom(eq(1L), any(UpdateRoomRequest.class)))
                .thenReturn(createRoomResponse());

        mockMvc.perform(put("/api/room/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                    "roomNumber": "101",
                    "name": "Deluxe V2",
                    "description": "อัปเดตแล้ว",
                    "capacity": 4,
                    "pricePerPetPerNight": 600
                    }
                    """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.roomNumber").value("101"));

        verify(roomService).updateRoom(eq(1L), any(UpdateRoomRequest.class));
    }

    @Test
    void setRoomStatusShouldReturnUpdatedStatus() throws Exception {
        RoomResponse maintenanceRoom = new RoomResponse(1L, "101", "Deluxe", "ห้องมาตรฐาน",
                3, new BigDecimal("500"), RoomStatus.MAINTENANCE, null);
        when(roomService.setRoomStatus(eq(1L), any())).thenReturn(maintenanceRoom);

        mockMvc.perform(patch("/api/room/1/status")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    { "status": "MAINTENANCE" }
                    """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("MAINTENANCE"));

        verify(roomService).setRoomStatus(eq(1L), any());
    }

    @Test
    void deactivateRoomShouldReturnInactiveRoom() throws Exception {
        RoomResponse inactiveRoom = new RoomResponse(1L, "101", "Deluxe", "ห้องมาตรฐาน",
                3, new BigDecimal("500"), RoomStatus.INACTIVE, null);
        when(roomService.deactivateRoom(1L)).thenReturn(inactiveRoom);

        mockMvc.perform(patch("/api/room/1/deactivate"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("INACTIVE"));

        verify(roomService).deactivateRoom(1L);
    }

    private RoomResponse createRoomResponse() {
        return new RoomResponse(
                1L,
                "101",
                "Deluxe",
                "ห้องมาตรฐาน",
                3,
                new BigDecimal("500"),
                RoomStatus.ACTIVE
                , null
        );
    }

        @Test
    void createRoomShouldReturnBadRequestWhenCapacityIsZero() throws Exception {
        mockMvc.perform(post("/api/room")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    { "roomNumber": "101", "name": "Deluxe",
                    "capacity": 0, "pricePerPetPerNight": 500 }
                    """))
                .andExpect(status().isBadRequest());

        verify(roomService, never()).createRoom(any());
    }

    @Test
    void createRoomShouldReturnBadRequestWhenPriceIsNegative() throws Exception {
        mockMvc.perform(post("/api/room")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    { "roomNumber": "101", "name": "Deluxe",
                    "capacity": 3, "pricePerPetPerNight": -1 }
                    """))
                .andExpect(status().isBadRequest());

        verify(roomService, never()).createRoom(any());
    }

        @Test
    void getRoomByIdShouldReturnNotFoundWhenRoomMissing() throws Exception {
        when(roomService.getRoomById(99L))
                .thenThrow(new ResourceNotFoundException("Room", 99L));

        mockMvc.perform(get("/api/room/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    void createRoomShouldReturnConflictWhenRoomNumberDuplicate() throws Exception {
        when(roomService.createRoom(any()))
                .thenThrow(new DuplicateResourceException("Room number already exists: 101"));

        mockMvc.perform(post("/api/room")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    { "roomNumber": "101", "name": "Deluxe",
                    "capacity": 3, "pricePerPetPerNight": 500 }
                    """))
                .andExpect(status().isConflict());
    }
}