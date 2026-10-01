package com.example.petshotel.controller.web;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.Test;
import static org.mockito.Mockito.when;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import com.example.petshotel.config.SecurityConfig;
import com.example.petshotel.domain.enums.RoomStatus;
import com.example.petshotel.dto.response.RoomResponse;
import com.example.petshotel.service.RoomService;

@WebMvcTest(HomeController.class)
@Import(SecurityConfig.class)
class HomeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private RoomService roomService;

    private RoomResponse room(Long id) {
        return new RoomResponse(id, "10" + id, "Room " + id, null, 2,
                new BigDecimal("500.00"), RoomStatus.ACTIVE);
    }

    @Test
    void home_showsAtMostThreeActiveRooms() throws Exception {
        when(roomService.getActiveRooms())
                .thenReturn(List.of(room(1L), room(2L), room(3L), room(4L)));

        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(view().name("index"))
                .andExpect(model().attribute("rooms", List.of(room(1L), room(2L), room(3L))));
    }
} 