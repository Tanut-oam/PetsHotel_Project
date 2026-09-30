package com.example.petshotel.controller.web;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.example.petshotel.domain.enums.RoomStatus;
import com.example.petshotel.dto.request.CreateRoomRequest;
import com.example.petshotel.dto.request.UpdateRoomRequest;
import com.example.petshotel.dto.request.UpdateStatusRequest;
import com.example.petshotel.dto.response.RoomResponse;
import com.example.petshotel.service.RoomService;

@WebMvcTest(RoomController.class)
@WithMockUser(roles = "ADMIN")
class RoomControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private RoomService roomService;

    private RoomResponse sampleRoom(Long id, RoomStatus status) {
        return new RoomResponse(id, "101", "Deluxe", "ห้องกว้าง", 3,
                new BigDecimal("500.00"), status);
    }

    // ===== ลูกค้า =====

    @Test
    void publicList_showsActiveRoomsOnly() throws Exception {
        List<RoomResponse> rooms = List.of(sampleRoom(1L, RoomStatus.ACTIVE));
        when(roomService.getActiveRooms()).thenReturn(rooms);

        mockMvc.perform(get("/rooms"))
                .andExpect(status().isOk())
                .andExpect(view().name("rooms"))
                .andExpect(model().attribute("rooms", rooms));

        verify(roomService).getActiveRooms();
    }

    @Test
    void detail_showsRoom() throws Exception {
        RoomResponse room = sampleRoom(1L, RoomStatus.ACTIVE);
        when(roomService.getRoomById(1L)).thenReturn(room);

        mockMvc.perform(get("/rooms/1"))
                .andExpect(status().isOk())
                .andExpect(view().name("room-detail"))
                .andExpect(model().attribute("room", room));
    }

    // ===== Admin =====

    @Test
    void adminList_showsAllRooms() throws Exception {
        List<RoomResponse> rooms = List.of(
                sampleRoom(1L, RoomStatus.ACTIVE),
                sampleRoom(2L, RoomStatus.MAINTENANCE));
        when(roomService.getAllRooms()).thenReturn(rooms);

        mockMvc.perform(get("/admin/rooms"))
                .andExpect(status().isOk())
                .andExpect(view().name("rooms/list"))
                .andExpect(model().attribute("rooms", rooms));
    }

    @Test
    void newRoomForm_returnsFormView() throws Exception {
        mockMvc.perform(get("/admin/rooms/new"))
                .andExpect(status().isOk())
                .andExpect(view().name("rooms/form"));
    }

    @Test
    void createRoom_success_redirectsWithMessage() throws Exception {
        mockMvc.perform(post("/admin/rooms")
                        .with(csrf())
                        .param("roomNumber", "101")
                        .param("name", "Deluxe")
                        .param("description", "ห้องกว้าง")
                        .param("capacity", "3")
                        .param("pricePerPetPerNight", "500.00"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/rooms"))
                .andExpect(flash().attribute("message", "สร้างห้องสำเร็จ"));

        ArgumentCaptor<CreateRoomRequest> captor = ArgumentCaptor.forClass(CreateRoomRequest.class);
        verify(roomService).createRoom(captor.capture());
        CreateRoomRequest req = captor.getValue();
        org.junit.jupiter.api.Assertions.assertEquals("101", req.roomNumber());
        org.junit.jupiter.api.Assertions.assertEquals("Deluxe", req.name());
        org.junit.jupiter.api.Assertions.assertEquals(3, req.capacity());
        org.junit.jupiter.api.Assertions.assertEquals(new BigDecimal("500.00"), req.pricePerPetPerNight());
    }

    @Test
    void createRoom_duplicateNumber_redirectsWithError() throws Exception {
        doThrow(new IllegalArgumentException("Room number already exists: 101"))
                .when(roomService).createRoom(any(CreateRoomRequest.class));

        mockMvc.perform(post("/admin/rooms")
                        .with(csrf())
                        .param("roomNumber", "101")
                        .param("name", "Deluxe")
                        .param("capacity", "3")
                        .param("pricePerPetPerNight", "500"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/rooms"))
                .andExpect(flash().attribute("error", "Room number already exists: 101"));
    }

    @Test
    void createRoom_withoutCsrf_isForbidden() throws Exception {
        mockMvc.perform(post("/admin/rooms")
                        .param("roomNumber", "101"))
                .andExpect(status().isForbidden());
    }

    @Test
    void editRoomForm_showsExistingRoom() throws Exception {
        RoomResponse room = sampleRoom(1L, RoomStatus.ACTIVE);
        when(roomService.getRoomById(1L)).thenReturn(room);

        mockMvc.perform(get("/admin/rooms/1/edit"))
                .andExpect(status().isOk())
                .andExpect(view().name("rooms/edit"))
                .andExpect(model().attribute("room", room));
    }

    @Test
    void updateRoom_success_redirectsWithMessage() throws Exception {
        mockMvc.perform(post("/admin/rooms/1")
                        .with(csrf())
                        .param("roomNumber", "102")
                        .param("name", "Deluxe Plus")
                        .param("description", "ปรับปรุงใหม่")
                        .param("capacity", "4")
                        .param("pricePerPetPerNight", "600"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/rooms"))
                .andExpect(flash().attribute("message", "แก้ไขห้องสำเร็จ"));

        verify(roomService).updateRoom(eq(1L), any(UpdateRoomRequest.class));
    }

    @Test
    void updateRoom_roomNotFound_redirectsWithError() throws Exception {
        doThrow(new IllegalArgumentException("Room not found: 99"))
                .when(roomService).updateRoom(eq(99L), any(UpdateRoomRequest.class));

        mockMvc.perform(post("/admin/rooms/99")
                        .with(csrf())
                        .param("roomNumber", "102")
                        .param("name", "X")
                        .param("capacity", "2")
                        .param("pricePerPetPerNight", "300"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/rooms"))
                .andExpect(flash().attribute("error", "Room not found: 99"));
    }

    @Test
    void updateStatus_success_redirectsWithMessage() throws Exception {
        mockMvc.perform(post("/admin/rooms/1/status")
                        .with(csrf())
                        .param("status", "MAINTENANCE"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/rooms"))
                .andExpect(flash().attribute("message", "เปลี่ยนสถานะห้องสำเร็จ"));

        ArgumentCaptor<UpdateStatusRequest> captor = ArgumentCaptor.forClass(UpdateStatusRequest.class);
        verify(roomService).setRoomStatus(eq(1L), captor.capture());
        org.junit.jupiter.api.Assertions.assertEquals(RoomStatus.MAINTENANCE, captor.getValue().status());
    }

    @Test
    void deactivateRoom_success_redirectsWithMessage() throws Exception {
        mockMvc.perform(post("/admin/rooms/1/deactivate").with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/rooms"))
                .andExpect(flash().attribute("message", "ปิดใช้งานห้องสำเร็จ"));

        verify(roomService).deactivateRoom(1L);
    }

    @Test
    void deactivateRoom_roomNotFound_redirectsWithError() throws Exception {
        doThrow(new IllegalArgumentException("Room not found: 99"))
                .when(roomService).deactivateRoom(99L);

        mockMvc.perform(post("/admin/rooms/99/deactivate").with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/rooms"))
                .andExpect(flash().attribute("error", "Room not found: 99"));
    }
}