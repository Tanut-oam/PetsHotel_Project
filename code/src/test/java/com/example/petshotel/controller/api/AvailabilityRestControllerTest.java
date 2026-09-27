package com.example.petshotel.controller.api;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.example.petshotel.domain.entity.Room;
import com.example.petshotel.domain.enums.RoomStatus;
import com.example.petshotel.mapper.AvailabilityMapper;
import com.example.petshotel.mapper.RoomMapper;
import com.example.petshotel.service.AvailabilityService;

class AvailabilityRestControllerTest {

    private AvailabilityService availabilityService;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        availabilityService = mock(AvailabilityService.class);

        AvailabilityRestController controller =
                new AvailabilityRestController(availabilityService, new AvailabilityMapper(new RoomMapper()));

        mockMvc = MockMvcBuilders
                .standaloneSetup(controller)
                .build();
    }

    @Test
    void findAvailableRoomsShouldReturnRooms() throws Exception {
        Room room = new Room();
        room.setId(1L);
        room.setRoomNumber("A101");
        room.setName("Deluxe");
        room.setCapacity(3);
        room.setPricePerPetPerNight(new BigDecimal("500.00"));
        room.setStatus(RoomStatus.ACTIVE);

        when(availabilityService.findAvailableRooms(LocalDate.of(2026, 10, 1), LocalDate.of(2026, 10, 3), 2))
                .thenReturn(List.of(room));

        mockMvc.perform(get("/api/room/available")
                .param("checkIn", "2026-10-01")
                .param("checkOut", "2026-10-03")
                .param("petCount", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.petCount").value(2))
                .andExpect(jsonPath("$.availableRooms.length()").value(1))
                .andExpect(jsonPath("$.availableRooms[0].roomNumber").value("A101"));
    }

    @Test
    void findAvailableRoomsShouldReturnBadRequestWhenPetCountMissing() throws Exception {
        mockMvc.perform(get("/api/room/available")
                .param("checkIn", "2026-10-01")
                .param("checkOut", "2026-10-03"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(availabilityService);
    }

    @Test
    void findAvailableRoomsShouldReturnBadRequestWhenPetCountIsZero() throws Exception {
        mockMvc.perform(get("/api/room/available")
                .param("checkIn", "2026-10-01")
                .param("checkOut", "2026-10-03")
                .param("petCount", "0"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(availabilityService);
    }
}