package com.example.petshotel.controller.web;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.example.petshotel.domain.entity.Room;
import com.example.petshotel.domain.enums.RoomStatus;
import com.example.petshotel.mapper.AvailabilityMapper;
import com.example.petshotel.mapper.RoomMapper;
import com.example.petshotel.service.AvailabilityService;

class AvailabilityControllerTest {

    private AvailabilityService availabilityService;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        availabilityService = mock(AvailabilityService.class);
        AvailabilityController controller =
                new AvailabilityController(availabilityService, new AvailabilityMapper(new RoomMapper()));
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    void firstVisitShouldShowEmptyForm() throws Exception {
        mockMvc.perform(get("/rooms/available"))
                .andExpect(status().isOk())
                .andExpect(view().name("availability/search"))
                .andExpect(model().attributeDoesNotExist("result"));

        verifyNoInteractions(availabilityService);
    }

    @Test
    void searchShouldPutResultInModel() throws Exception {
        Room room = new Room();
        room.setId(1L);
        room.setRoomNumber("A101");
        room.setName("Deluxe");
        room.setCapacity(3);
        room.setPricePerPetPerNight(new BigDecimal("500.00"));
        room.setStatus(RoomStatus.ACTIVE);
        when(availabilityService.findAvailableRooms(LocalDate.of(2026, 10, 1), LocalDate.of(2026, 10, 3), 2))
                .thenReturn(List.of(room));

        mockMvc.perform(get("/rooms/available")
                .param("checkIn", "2026-10-01")
                .param("checkOut", "2026-10-03")
                .param("petCount", "2"))
                .andExpect(status().isOk())
                .andExpect(view().name("availability/search"))
                .andExpect(model().attributeExists("result"));
    }

    @Test
    void invalidDatesShouldShowErrorMessage() throws Exception {
        when(availabilityService.findAvailableRooms(any(), any(), anyInt()))
                .thenThrow(new IllegalArgumentException("วันที่ Check-out ต้องอยู่หลังวันที่ Check-in"));

        mockMvc.perform(get("/rooms/available")
                .param("checkIn", "2026-10-03")
                .param("checkOut", "2026-10-01")
                .param("petCount", "1"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("error", "วันที่ Check-out ต้องอยู่หลังวันที่ Check-in"));
    }
}